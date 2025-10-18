import os

def count_lines_of_code(root_dir, extension=".java"):
    total_lines = 0
    total_files = 0
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith(extension):
                total_files += 1
                filepath = os.path.join(subdir, file)
                with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
                    lines = f.readlines()
                    # Optionally exclude empty lines or comments here if you want
                    total_lines += len(lines)
    return total_files, total_lines

if __name__ == "__main__":
    folder = "."  # current directory
    files, lines = count_lines_of_code(folder)
    print(f"Found {files} '{'.java'}' files with a total of {lines} lines of code.")
