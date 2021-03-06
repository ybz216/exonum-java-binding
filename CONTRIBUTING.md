# Exonum Java Contribution Guide

Exonum Java is open to any contributions, whether 
it is a feedback on existing features, a request for a new one, a bug report
or a pull request. This document describes how to work with this project: 
  * how to [build](#how-to-build) it
  * how to [test](#tests) it
  * the [code style guidelines](#the-code-style)
  * how to [submit an issue](#submitting-issues)
  * how to [submit a PR](#submitting-pull-requests)

Such as this project has sub-projects with sub-modules it is possible
to build the whole project as well as any of the sub-projects separately.
This guide shows how to build the whole project only. 

## How to Build
### System Dependencies
You need to install the following dependencies:
  * Linux or macOS. Windows support is coming soon. <!-- TODO: Link Java roadmap when it is published -->
  * [JDK 1.8+](https://jdk.java.net/).
  * [Maven 3.5+](https://maven.apache.org/download.cgi).
  * [Stable Rust compiler](https://www.rust-lang.org/).
    The minimum supported version is Rust 1.41.0.
  * The [system dependencies](https://exonum.com/doc/version/0.13-rc.2/get-started/install/) of Exonum. 
  You do _not_ need to manually fetch and compile Exonum.

  __Important__: It is necessary to install RocksDB
  package and to set the environment variable `ROCKSDB_LIB_DIR`.
  To install the package on Ubuntu:
  
  ```bash
  sudo add-apt-repository ppa:exonum/rocksdb
  sudo apt-get update && sudo apt-get install librocksdb6.2
  export ROCKSDB_LIB_DIR=/usr/lib
  ```
  
  To install the package via Homebrew on Mac:
  
  ```bash
  brew install rocksdb
  export ROCKSDB_LIB_DIR=/usr/local/lib
  ```
  
  * For automatic packaging of the Exonum Java app you need [CMake](https://cmake.org/) installed in your system. 
  Also on Mac you need a [`coreutils`](https://formulae.brew.sh/formula/coreutils) package installed.

### Building
⚠️ __Important__: Keep in mind that project contains git submodules 
and extra steps are required to init and/or update them:
  * `git submodule update --init` before the first build
  * `git submodule update` when the submodule revision changes.

Set required environment variables, once in a shell you use to build the project:
```$sh
$ source exonum-java-binding/tests_profile
```
Then run:
```$sh
$ ./run_all_tests.sh
```

#### Building Exonum Java App

Run:

```$sh
$ cd exonum-java-binding
$ ./package_app.sh
```

This command will build and package Exonum Java app with all the necessary runtime dependencies
in a single `zip` archive in `exonum-java-binding/packaging/target` directory.

Before packaging, the script will also run all the tests to guarantee that the generated application
is valid. It may take a long time, so you can pass `--skip-tests` flag to skip tests running:

```$sh
$ ./package_app.sh --skip-tests
```

By default, the Exonum Java app is built in debug mode, which affects performance
and is not desired for production usage. To enable release mode, you need
to pass `--release` flag to the `package_app.sh` script.

#### (Optional) Maven Configuration
Each _independent_ module and the root aggregator project contain `.mvn` directory 
with Maven configuration files. 

- `jvm.config` contains extra JVM options that has [proved][build-benches] to speed up the build.
- `maven.config` contains default Maven command line flags (not included, ignored by Git). 
  `maven-template.config` is a template file with some configuration options that
  can be copied in a local `maven.config`. Some options that can be considered:
  - `--threads 1C` — will activate parallel builds. Independent modules will be built concurrently,
    with the given number of threads (here — 1 times the number of available cores).
  - `-Djunit.jupiter.execution.parallel.enabled=true` + 
    `-Djunit.jupiter.execution.parallel.mode.default=concurrent` — enable [parallel JUnit 5 test
    execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution).
  - `-Djacoco.skip` — if you never browse the local reports when 
    [running all tests](#running-tests).


<details>
<summary>Why `.mvn` is present in each independent module?</summary>

Remember that configuration files in `.mvn` are used when Maven is launched from 
its parent directory; or when you build a _child_ project in multi-module build, the
parent’s `.mvn` is used. For example, if you build `core`, `../.mvn` will be picked up because
`core` is a child of `ejb-parent`; but if you build `ejb-parent`, `../.mvn` will not have any
effect because `ejb-parent` is _not_ a child of `exonum-java-parent`.
</details>

See [Maven configuration documentation](https://maven.apache.org/configure.html) for details. 

[build-benches]: https://jira.bf.local/browse/ECR-534?focusedCommentId=48501&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-48501

#### Useful Commands

##### Clean Rust Targets using Maven

Run `mvn clean -DcleanRust`.

## EJB Modules
The [Exonum Java Binding](exonum-java-binding) project is split into several modules. 
Here are the main ones:
  * [`core`](exonum-java-binding/core) contains the APIs to define and implement an 
  [Exonum service](https://exonum.com/doc/version/0.13-rc.2/get-started/design-overview/#modularity-and-services).
  * [`core-native`](exonum-java-binding/core/rust) contains the glue code between Java and Rust.
  * [`app`](exonum-java-binding/core/rust/exonum-java) is an application that runs a node with Java 
  and Rust services.
  * [`common`](exonum-java-binding/common) provides common functionality to Exonum core
  and light clients: [Exonum proofs](https://exonum.com/doc/version/0.13-rc.2/get-started/design-overview/#proofs),
  hashing and cryptographic operations, serialization support.
  * [`exonum-service-archetype`](exonum-java-binding/service-archetype) implements an archetype
  generating a template project of Exonum Java service. 
  <!-- TODO: a link to a getting started guide/generating a project -->

## Tests
### Categories of Tests
There are several categories of tests:
  * Unit tests in Java and Rust modules.
  * Integration tests in Java, some of which require a native library.
  * Integration tests in Rust that require a JVM together with `ejb-core` artifact.
    Reside in [`core-native`](exonum-java-binding/core/rust/integration_tests).
  * System tests — these are currently performed internally 
    and use a [QA-service](exonum-java-binding/qa-service).

### Running Tests
<!-- TODO: Shall we explain what `mvn install` runs, and what `run_all_tests`? -->
For convenience, the tests are divided into several groups.
To run all tests, invoke this script:
```$sh
$ ./run_all_tests.sh
```
The following scripts can be run separately 
from the [EJB](exonum-java-binding) directory:
* `./run_maven_tests.sh` - all tests in Java and unit tests in Rust.
* `./run_native_integration_tests.sh` - integration tests in Rust.
* `./run_app_tests.sh` - application tests in Rust.

### Writing Tests
#### Java
Use JUnit + [Mockito](https://github.com/mockito/mockito) or hand-written fakes.
Currently there is no project-wide default for an assertion library: 
Hamcrest and AssertJ are used in various modules.

##### Integration Tests in Core
The integration tests in `core` are bound to `verify` phase of the Maven build. 
The name of IT classes must end with `IntegrationTest`. 
If your test verifies the behaviour of a class with native methods, 
or depends on such classes, the native library is required, 
which can be loaded with `LibraryLoader.load()`.

IntelliJ IDEA infers the JVM arguments from the `pom.xml` and runs ITs just fine.
If you use another IDE, configure it to pass `-Djava.library.path` system property 
to the JVM when running tests. For more details, see the failsafe plugin 
[configuration](exonum-java-binding/core/pom.xml).

#### Rust
All tests require several environment variables to be set.
These variables with the brief explanation can be found in the `./tests_profile` script.
They can be imported into the current shell with `source ./tests_profile`.
It’s more convenient to use the corresponding `./run_XXX_tests.sh` script.

## The Code Style
### Java
Java code must follow the [Google code style](https://google.github.io/styleguide/javaguide.html).
[Checkstyle](http://checkstyle.sourceforge.net/index.html) checks the project 
during `validate` phase (i.e., _before_ compilation) of the build. You can also run it manually:
```$sh
$ mvn validate
```

Development builds only report violations, pass `-P ci-build` to fail the build in case of violations.

### Rust
Rust code follows the [Rust style guide](https://github.com/rust-lang-nursery/fmt-rfcs/blob/master/guide/guide.md).
[`rustfmt`](https://github.com/rust-lang-nursery/rustfmt) enforces the code style.

After installation, you can run it with
```$sh
$ cd exonum-java-binding/core/rust
$ cargo +stable fmt --all -- --check
```

## Submitting Issues
Use Github Issues to submit an issue, whether it is a question, some feedback, a bug or a feature request:
https://github.com/exonum/exonum-java-binding/issues/new

JIRA is for internal use so far and is not publicly available yet.

## Submitting Pull Requests
Before starting to work on a PR, please submit an issue describing the intended changes.
Chances are — we are already working on something similar. If not — we can then offer some
help with the requirements, design, implementation or documentation.

It’s fine to open a PR as soon as you need any feedback — ask any questions in the description.

<!-- todo: Add licensing information/CLA -->
