# Bosslint Development Guide for AI Agents

## Project Overview

Bosslint is a Clojure CLI tool that runs multiple linters against Git-changed
files. It detects which files changed between Git refs, determines their types,
and dispatches appropriate linters. The binary is compiled to a native
executable via GraalVM native-image.

## Build & Test Commands

```sh
# Run all tests
clojure -X:test

# Build native binary (clean + uberjar + native-image, requires $GRAALVM_HOME)
clojure -T:build bin

# Individual build steps
clojure -T:build clean
clojure -T:build uber         # produces target/bosslint.jar
clojure -T:build native-image # requires $GRAALVM_HOME
```

The project uses Clojure CLI tools (`deps.edn`) with `tools.build`. There is no
separate lint or format command — the project itself is the linter runner.

## Architecture

### Linter Abstraction

The core pattern is the `deflinter` macro in `src/bosslint/linter.clj`. Each
linter is defined with three multimethods:

- `name` - human-readable display name
- `files` - selects relevant files from the file-group (files grouped by type keyword)
- `lint` - runs the linter, returns `:success`, `:warning`, `:error`, or `nil` (skipped)

The macro also registers the linter keyword into the `:bosslint/linter`
hierarchy via `derive`, which is how `main.clj` discovers all linters.

### File Type Dispatch

`linter.clj` has a `path-type-pairs` map of regex patterns to type keywords
(`:clj`, `:yaml`, `:docker`, etc.). `path->types` maps a file path to a set of
type keywords. Files are grouped by type, then each linter's `files` method
selects its relevant subset via `select-files`.

### Flow

`main.clj` → `run-check`: enumerate changed files via git → group by file type →
filter enabled linters → for each linter, select matching files and run →
aggregate statuses → exit code (0/1/2).

### Key Source Files

- `src/bosslint/main.clj` — entry point, CLI parsing, check/linters commands
- `src/bosslint/linter.clj` — `deflinter` macro, file type dispatch, multimethods
- `src/bosslint/git.clj` — git diff/ls-files/top-dir operations
- `src/bosslint/process.clj` — process execution, `command-exists?` (memoized)
- `src/bosslint/config.clj` — EDN config file loading
- `src/bosslint/linter/` — individual linter implementations

## Adding a New Linter

1. Create `src/bosslint/linter/<name>.clj` with a `deflinter` form
2. Require the new namespace in `src/bosslint/main.clj`'s ns form
3. If it handles a new file type, add the regex→keyword to `path-type-pairs` and the keyword to `file-type-set` in `linter.clj`
4. Update `example/config.edn`

## Communication

- Issue and PR titles and descriptions must be written in English

## Code Style

- Formatted with cljfmt (config in `.cljfmt.edn`, notably custom indent for `deflinter`)
- Snake_case filenames map to kebab-case namespace segments (e.g., `clj_kondo.clj` → `bosslint.linter.clj-kondo`)
