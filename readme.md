# Git Historical Stats

An iterative commandline tool to generate statistics on long-term projects. This tools consists of three steps:

1. Get "important" commits.
2. Calculate measurements for those commits.
3. Generate charts on those measurements.

These charts can be useful when you're doing long-term migrations which can't be done overnight, but you want to keep progress.

It's iterative, so the second time this script runs, it will only calculate the latest commits and new measurements and
charts.

---

As an example, there are three samples from Google IO's Schedule app (https://github.com/google/iosched), which is
updated each year with the latest technologies. One odd thing in this repo is that it's not an entire-year process, but gets only worked on for a few months. Therefore you can see that the last commit of 2018 was in August.

1. Lines of code per year.
You can see that in 2018 the app was rewritten in Kotlin and that a lot less code was needed.
   ![Google IOSched Lines of code per year](https://github.com/nielsz/project-info/blob/main/screenshots/google_iosched_cloc_year.png?raw=true)


2. Number of files that contain `import org.junit.Test` and number of `@Test` annotations.    ![Google IOSched Junit4](https://github.com/nielsz/project-info/blob/main/screenshots/google_iosched_junit4_year.png?raw=true)


3. Activities and Fragments. Google has moved towards Fragments without Activities for the last few years now.   ![Google IOSched lifecycle](https://github.com/nielsz/project-info/blob/main/screenshots/google_iosched_lifecycle_year.png?raw=true)

## Installation
1. Download the latest release, and place it somewhere. `~/gitHistoricalStats` would be nice.
2. Run `./gitHistoricalStats` within the `bin` directory. It will create directories `repos`,`projects`, and `output`. 
3. Do a new clone of your project to the `repos` directory. This is because this script will checkout all the important commits, and does some `git reset --hard HEAD` at the beginning and the end. You want to avoid running this script while there are uncommitted changes. 
4. Add a `projects/myproject.config.json` with the following structure:
```
{
   "repo": "/Users/username/gitHistoricalStats/repos/myproject",
   "branch": "develop",
   "filetypes":["kt","java"],
   "charts":[],
   "measurements":[]
   }
```
## Usage
Run `./gitHistoricalStats --project=myproject` from the `bin` directory. It will run all the measurements and charts.<br/>
Run `./gitHistoricalStats --project=myproject --runAllMeasurements` to rerun all the measurements, even if they were already done.<br/>
Run `./gitHistoricalStats --project=myproject --rerunMeasurement=junit4imports` to rerun the measurement `junit4imports`, even if this was already done.<br/>

If there are measurements and charts defined, the charts will be stored in the `output` directory.

---
## Step 1

Based on the config file which contains the path to the local repo and the branch, it will run a git log, and will store
the important commits in the data file. A commit will be considered important if it's the first commit, the last commit,
or the last commit of a month, quarter or year.

## Step 2

For each commit, it will run measurements. A measurement can be cloc, a simple grep call, or a custom written bash
script.

#### Type: CLOC

This calculates the sum of all files that have the extension `.kt`

```
{
      "type": "cloc",
      "key": "cloc_kotlin",
      "filetypes": [
        "kt"
      ]
    }
```

#### Type: GREP

This calculates the amount of times a certain grep is found in the codebase.

```
{
      "type": "grep",
      "key": "junit4imports",
      "pattern": "import org.junit.Test"
    }
```

#### Type: BASH

This allows you to run your own custom scripts to calculate something. It should always output a number. The following
example calculates the amount of Android Activities.

```
{
      "type": "bash",
      "key": "activities",
      "command": "git ls-files | grep \"AndroidManifest.xml\" | xargs cat | grep \"<activity\" | wc -l"
    }
```

## Step 3

Based on the measurements, it's possible to generate charts. For each chart, it will generate five charts: Yearly,
Quarterly, Monthly, last 12 quarters and last 12 months. The output is always a bar chart. It can have multiple bars, and each bar can be a stacked bar as
well.

Display the number of JUnit4 imports:

```
{
      "id": "junit4",
      "title": "JUnit 4",
      "items": [
        ["junit4imports"]
      ]
    }
```

Display Junit4 vs Junit5 as two separate bars:

```
{
    "id": "junit4_5",
    "title": "JUnit 4 vs Junit 5",
    "subtitle": "Comparing the legacy vs new variants",
    "caption": "By Nielsz",
    "items": [
      ["junit4imports"],
      ["junit5imports"]
    ]
}
```

Stackable charts: All versions of rxjava are stacked and placed as one bar next to Kotlin Coroutines. Optionally, this
chart has a customized legend.

```
{
    "id": "reactive",
    "title": "Reactive programming",
    "items": [
      ["rxjava1", "rxjava2", "rxjava3"],
      ["kotlincoroutines"]
    ],
    "legend": {
        "title": "Reactive",
        "items": [
          "RxJava 1",
          "RxJava 2",
          "RxJava 3",
          "Coroutines"
        ]
      }
}
```

## Config

Example config:

```
{
  "repo": "/local/path/to/repo",
  "branch": "develop",
  "filetypes": ["kt","java"],
  "measurements": [
    {
      "type": "grep",
      "key": "junit4imports",
      "pattern": "import org.junit.Test"
    }
  ],
  "charts": [
    {
      "id": "junit4",
      "title": "JUnit 4",
      "items": [
        ["junit4imports"]
      ]
    }
  ]
}
```
