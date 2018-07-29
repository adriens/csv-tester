# Purpose

The purpose of this dummy test project is to prototype how to tests
file quality check during the build process...it's like some linting...but dedicated forcsv files.

For now, this code focuses on getting well formated csv files, respecting the ```RFC4180```. I did trigger
this need to check as part of the build that the csv meet our requirements instead of adding constraints
to the ```CODE_OF_CONDUCT.md```. Hence the quality is guaranted as part of the build : it's much
easier for everyone.

The project scans the files and causes the run to fail if any csv does not meet our requirements.

As a result, since the tests are passing, you have the guarantee that they will nicely be displayed on Github,
which is really very convenient.

# Further

The first approach was to develop as fast a possible something that would protect our master Branch
with Travis build against malformated csv files.

If you feel that a maven plugin could be a nice feature : tell it.
If you feel that a more advanced csv linter should be implemented : tell it (or code and donate it).
If you have any other id on how to lint maven project source files : give your ideas ...or donate some code.

# linkedIn article

A dedicated linkedIn article is available here : https://www.linkedin.com/pulse/unit-test-vs-rules-lints-ci-daedalus-laws-adrien-sales/
