import os

def count_java_stats(folder_path):
    total_lines = 0
    total_classes = 0
    total_characters = 0

    for root, _, files in os.walk(folder_path):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    for line in f:
                        stripped = line.strip()
                        if stripped and not stripped.startswith("//"):  # Exclude blank lines and single-line comments
                            total_lines += 1
                        if 'class ' in stripped:  # Rough class count
                            total_classes += 1
                        total_characters += len(line)

    return total_lines, total_classes, total_characters

if __name__ == "__main__":
    plugins_folder = "Plugins"  # Change if your folder is elsewhere
    lines, classes, characters = count_java_stats(plugins_folder)
    print(f"Total lines of code: {lines}")
    print(f"Total Java classes: {classes}")
    print(f"Total characters: {characters}")
