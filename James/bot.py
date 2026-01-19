import discord
from discord import app_commands
from discord.ext import tasks
import aiohttp
import json
from datetime import datetime, timezone

with open("config.json", "r") as f:
    config = json.load(f)

DATA_FILE = "data.json"

def load_data():
    try:
        with open(DATA_FILE, "r") as f:
            return json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        return {
            "channel_id": None,
            "notification_channel_id": None,
            "role_id": None,
            "last_version_id": None,
            "last_downloads": 0
        }

def save_data(data):
    with open(DATA_FILE, "w") as f:
        json.dump(data, f, indent=2)

data = load_data()

PROJECT_ID = config["project_id"]

intents = discord.Intents.default()
client = discord.Client(intents=intents)
tree = app_commands.CommandTree(client)

@client.event
async def on_ready():
    await tree.sync()
    check_modrinth.start()

@tree.command(name="setchannel", description="Set the channel for Modrinth release notifications")
@app_commands.checks.has_permissions(manage_guild=True)
async def setchannel(interaction: discord.Interaction):
    data["channel_id"] = interaction.channel_id
    save_data(data)
    await interaction.response.send_message(
        f"Release channel set to {interaction.channel.mention}",
        ephemeral=True
    )

@tree.command(name="setrole", description="Set the role to ping on new releases")
@app_commands.checks.has_permissions(manage_guild=True)
async def setrole(interaction: discord.Interaction, role: discord.Role):
    data["role_id"] = role.id
    save_data(data)
    await interaction.response.send_message(
        f"Role ping set to {role.mention}",
        ephemeral=True
    )

@tree.command(
    name="notificationchannel",
    description="Set the channel for download notifications"
)
@app_commands.checks.has_permissions(manage_guild=True)
async def notificationchannel(interaction: discord.Interaction):
    data["notification_channel_id"] = interaction.channel_id
    save_data(data)
    await interaction.response.send_message(
        f"Download notifications will be sent in {interaction.channel.mention}",
        ephemeral=True
    )

@tasks.loop(minutes=config["check_interval_minutes"])
async def check_modrinth():
    try:
        async with aiohttp.ClientSession() as session:
            release_channel_id = data.get("channel_id")
            if release_channel_id:
                version_url = f"https://api.modrinth.com/v2/project/{PROJECT_ID}/version"
                async with session.get(version_url) as resp:
                    if resp.status == 200:
                        versions = await resp.json()
                        if versions:
                            latest = versions[0]

                            if latest["id"] != data.get("last_version_id"):
                                data["last_version_id"] = latest["id"]
                                save_data(data)

                                if latest.get("version_type") == "release":
                                    channel = client.get_channel(release_channel_id)
                                    if channel:
                                        changelog = latest.get("changelog") or "No changelog provided."
                                        changelog = changelog.strip()

                                        if len(changelog) > 4000:
                                            changelog = (
                                                changelog[:4000]
                                                + "...\n\n"
                                                f"**[View Full Changelog](https://modrinth.com/project/{PROJECT_ID}/version/{latest['id']})**"
                                            )

                                        embed = discord.Embed(
                                            title=f"New Release: v{latest['version_number']}",
                                            url=f"https://modrinth.com/project/{PROJECT_ID}/version/{latest['id']}",
                                            description=changelog,
                                            color=0x00FF7F
                                        )

                                        embed.set_footer(
                                            text="Released on Modrinth",
                                            icon_url="https://modrinth.com/favicon.ico"
                                        )

                                        try:
                                            embed.timestamp = datetime.fromisoformat(
                                                latest["date_published"].replace("Z", "+00:00")
                                            )
                                        except:
                                            embed.timestamp = datetime.now(timezone.utc)

                                        role_id = data.get("role_id")
                                        content = f"<@&{role_id}>" if role_id else None

                                        await channel.send(content=content, embed=embed)

            notif_channel_id = data.get("notification_channel_id")
            if notif_channel_id:
                project_url = f"https://api.modrinth.com/v2/project/{PROJECT_ID}"
                async with session.get(project_url) as resp:
                    if resp.status == 200:
                        project = await resp.json()
                        downloads = project.get("downloads", 0)

                        last_downloads = data.get("last_downloads", 0)

                        if downloads > last_downloads:
                            increase = downloads - last_downloads
                            data["last_downloads"] = downloads
                            save_data(data)

                            channel = client.get_channel(notif_channel_id)
                            if channel:
                                await channel.send(
                                    f"📥 **New downloads!**\n"
                                    f"+{increase:,} downloads\n"
                                    f"**Total:** {downloads:,}\n"
                                    f"https://modrinth.com/project/{PROJECT_ID}"
                                )

    except Exception as e:
        print(f"Modrinth check failed: {e}")

client.run(config["token"])


