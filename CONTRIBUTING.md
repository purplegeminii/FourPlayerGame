Contributing to FourPlayerGame
=================================

Thank you for your interest in contributing! This file explains how to report issues, propose changes, and submit pull requests so we can review and accept contributions quickly.

1) Report issues
-----------------
- Use the repository's issue tracker to report bugs or suggest enhancements. Provide a short title, a clear description, and reproduction steps when possible.
- Include the Java version (`java -version`) and any error output or stack traces.

2) Fork, Branch, and Implement
-------------------------------
- Fork the repository and create a descriptive branch for your change, e.g. `feature/auto-play-potions` or `fix/equipment-slot-loading`.
- Keep changes small and focused: one feature or bugfix per pull request.

3) Build & Test Locally
------------------------
Compile the project and run a short auto-play to exercise behavior:

```bash
cd /Users/purplegeminii/Desktop/FourPlayerGame
javac src/*.java -d out
java -cp out Main auto 20 5
```

Run interactive mode to manually verify UI flows:

```bash
java -cp out Main
```

4) Coding style and guidance
---------------------------
- This is a small, educational project. Favor clarity and small, well-tested changes.
- Keep public APIs stable and avoid large refactors unless they come with tests or clear justification.
- The project uses a single shared `Scanner` (`Main.input`) — do not create new `Scanner(System.in)` instances.

5) Configuration and runtime files
----------------------------------
- `config/equipment_slots.properties` contains substring -> slot mappings for equipment names. Editing this file does not require code changes and is a valid contribution.
- Save files: `save_progress.json` and `save_auto_progress.json` are created at runtime; do not add them to commits.

6) Submitting a Pull Request
----------------------------
- Push your branch to your fork and open a Pull Request against `master` with a clear description and rationale.
- Describe any manual steps needed to test your change and include sample commands.

7) License
----------
By contributing you accept that your contributions will be licensed under the project's MIT license. See `LICENSE` for details.

8) Contact
----------
If you want help with a proposed change before implementing it, open an issue or reach out in the repository's discussion area.

Thank you — contributions make this project better!
