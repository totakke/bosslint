# bosslint

![build](https://github.com/totakke/bosslint/workflows/build/badge.svg)

Good health is the most important thing.

## Installation

### Build

```console
$ clojure -Anative-image
$ chmod +x bosslint
$ mv bosslint [/your/PATH/dir/]
```

## Usage

```console
$ bosslint check HEAD~1
clj-kondo:
...
cljfmt:
...
stylelint:
...
```

### Configuration

bosslint implicitly loads `~/.bosslint/config.edn` file.

Alternatively, you can specify a configuration file with `--config` option.

```console
$ bosslint check --config [/path/to/config.edn] master
```

Look at the [configuration example](example/config.edn).

### Supported Linters

- [clj-kondo](https://github.com/borkdude/clj-kondo)
- [checkstyle](https://checkstyle.org/)
- [cljfmt](https://github.com/weavejester/cljfmt)
- [eastwood](https://github.com/jonase/eastwood)
- [flake8](https://flake8.pycqa.org/)
- [hadolint](https://github.com/hadolint/hadolint)
- [stylelint](https://stylelint.io/)
- [Swiftlint](https://realm.github.io/SwiftLint/)

## License

Copyright © 2020 [Toshiki Takeuchi](https://totakke.net/)

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
