# Bosslint

[![build](https://github.com/totakke/bosslint/actions/workflows/build.yml/badge.svg)](https://github.com/totakke/bosslint/actions/workflows/build.yml)

One command to run multiple linters under Git version control

> Good health is the most important thing. More than success, more than money,
> more than power.
>
> &mdash; Hyman Roth / The Godfather Part II

## Preview

https://github.com/user-attachments/assets/addd6a48-93a9-4748-b932-c1b952ef90bf

## Installation

### Brew (MacOS and Linux)

```sh
brew install totakke/tap/bosslint
```

### Manual install

OS & arch:

- `os`:
  - Mac OS - `macos`
  - Linux - `linux`
- `arch`:
  - amd64 / x86_64  - `amd64`
  - aarch64 / arm64  - `aarch64`

Download & install:

```sh
curl -sSL https://github.com/totakke/bosslint/releases/download/v0.6.120/bosslint_[os]_[arch] -o bosslint
chmod +x bosslint
mv bosslint [/your/PATH/dir/]
```

### Build

To build Bosslint yourself, you must install [Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools),
[GraalVM](https://www.graalvm.org/latest/getting-started/#installing) and
GraalVM's `native-image`. Additionally, `GRAALVM_HOME` environment variable must
be set.

```sh
clojure -T:build native-image
mv bosslint [/your/PATH/dir/]
```

## Usage

### Basics

`bosslint check` command checks changed files between commits, commit and
working tree, etc. with appropriate linters. Arguments of the ref are similar to
[`git-diff`](https://git-scm.com/docs/git-diff) arguments.

```console
$ bosslint check HEAD master
clj-kondo:
linting took 281ms, errors: 0, warnings: 0
==> Success ✅

cljfmt:
All source files formatted correctly
==> Success ✅

eastwood:
...
== Linting done in 1887 ms ==
== Warnings: 0 (not including reflection warnings)  Exceptions thrown: 0
==> Success ✅
```

You can use `:all` to check all files under a Git project.

```sh
bosslint check :all
```

Bosslint just dispatches each linter but does not contain any linters
themselves. You must install each linter preliminarily. Supported linters are
shown by `bosslint linters` command or are found in
[Supported linters](#supported-linters) section.

### Supported linters

| Linter | Bosslint linter name | Target files |
| ------ | -------------------- | ------------ |
| [actionlint](https://rhysd.github.io/actionlint/) | `actionlint` | `.github/workflows/*.{yaml,yml}` |
| [Checkstyle](https://checkstyle.org/) | `checkstyle` | `**/*.java` |
| [clj-kondo](https://github.com/borkdude/clj-kondo) | `clj-kondo` | `**/*.{clj,cljc,cljs}` |
| [cljfmt](https://github.com/weavejester/cljfmt) | `cljfmt` | `**/*.{clj,cljc,cljs}` |
| [cljstyle](https://github.com/greglook/cljstyle) | `cljstyle` | `**/*.{clj,cljc,cljs}` |
| [dartanalyzer](https://dart.dev/tools/dartanalyzer) | `dartanalyzer` | `**/*.dart` |
| [Eastwood](https://github.com/jonase/eastwood) | `eastwood` | `**/*.{clj,cljc}` |
| [Flake8](https://flake8.pycqa.org/) | `flake8` | `**/*.py` |
| [hadolint](https://github.com/hadolint/hadolint) | `hadolint` | `**/Dockerfile` |
| [jsonlint](https://github.com/zaach/jsonlint) | `jsonlint` | `**/*.json` |
| [kubeval](https://www.kubeval.com/) | `kubeval` | `**/*.{yaml,yml}` |
| [markdownlint-cli](https://github.com/igorshubovych/markdownlint-cli) | `markdownlint` | `**/*.{markdown,md}` |
| [ShellCheck](https://www.shellcheck.net/) | `shellcheck` | `**/*.sh` |
| [sql-lint](https://github.com/joereynolds/sql-lint) | `sql-lint` | `**/*.sql` |
| [stylelint](https://stylelint.io/) | `stylelint` | `**/*.{css,sass,scss}` |
| [SwiftLint](https://realm.github.io/SwiftLint/) | `swiftlint` | `**/*.swift` |
| [tflint](https://github.com/terraform-linters/tflint) | `tflint` | `**/*.tf` |
| [yamllint](https://yamllint.readthedocs.io/) | `yamllint` | `**/*.{yaml,yml}` |

### Configuration

Bosslint implicitly finds and loads a configuration file in the following order.

1. `$XDG_CONFIG_HOME/bosslint/config.edn`
2. `$HOME/.config/bosslint/config.edn`
3. `$HOME/.bosslint/config.edn`

Alternatively, you can specify a configuration file with `--config` option.

```sh
bosslint check --config path/to/config.edn ref1 ref2
```

The configuration file must be written in Extensible Data Notation (EDN) format,
e.g.,

```clojure
{:clj-kondo
 {:disabled? false}

 :cljfmt
 {:version "0.13.0"
  :clojure {:command-options ["--indents" "/path/to/indentation.edn"]}}

 :eastwood
 {:disabled? true
  :version "1.4.3"}}
```

The configuration file has a single map including linter keys and config vals.
All linter config supports `:disabled?` option (default `false`). For specific
options to a linter, look at the [configuration example](example/config.edn).

### Exit codes

Exit codes of `bosslint check` command are based on the results of all executed
linters.

- `0`: no linters output errors and warnings
- `1`: one or more linters output errors, or an error occurs in main Bosslint process
- `2`: one or more linters output warnings

## License

Copyright © 2020 [Toshiki Takeuchi](https://totakke.net/)

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
<http://www.eclipse.org/legal/epl-2.0>.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at <https://www.gnu.org/software/classpath/license.html>.
