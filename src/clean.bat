@echo off
set "dir_path=."

echo Deleting *.class files in %dir_path% and its subdirectories...
for /r "%dir_path%" %%f in (*.class) do del /q "%%f"

echo All *.class files have been deleted.