# DeepFocusCrawler

A Java desktop crawler I built back in 2013: you give it a keyword, it asks Google's Custom Search API for seed URLs, then recursively follows links from those pages (up to a depth you set) and saves every page that matches your keyword to disk.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.java.com)

### Key Characteristics

- **Intelligent Search**: Leverages Google Custom Search API for initial URL discovery
- **Recursive Extraction**: Automatically extracts and processes nested links with customizable depth limits
- **Multi-language Support**: Supports 45+ languages for targeted searches
- **Real-time Monitoring**: Live progress tracking with UI updates
- **Respect for Web Standards**: robots.txt compliance and configurable request delays
- **Efficient Queue Management**: Smart duplicate detection and queue processing

---

## Heads up — old code

This is a 2013 project I'm dusting off, not something freshly written. A few things to know before you dig in:

- **Everything is named in French** (`lancerrecherche`, `chaineDeRecherche`, `profondeur`...). If you want to contribute, renaming these to English is genuinely one of the most useful things you could do.
- It hasn't been tested against modern Java (11/17/21). It'll probably need work.
- The HTML parsing library (`htmlparser`) is ancient and deprecated.
- No request throttling out of the box — see the note at the bottom before pointing this at anything real.

Bug reports, PRs, and "this doesn't compile on Java 17" issues are all welcome.

## How it's put together

The whole thing follows a fairly classic Swing app shape: a GUI, a coordinator in the middle, and a few workers underneath, wired together with an Observer/Observable pattern instead of callbacks.

```
Fenetre (GUI)  ──observes──>  Manager
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
             GoogleRecherche  Extracteur    Sauvegarde
              (seed URLs)   (parse+links)   (save to disk)
```

**Manager** is the brain. You give it a keyword, a language, a depth and an output folder; it asks `GoogleRecherche` for the first batch of URLs, then works through a queue: pull a URL, hand it to `Extracteur`, get back the page text and its outgoing links, push the new links back onto the queue if we haven't hit max depth, repeat. It also checks each URL's content type before bothering to parse it, so it doesn't choke on PDFs or images.

**GoogleRecherche** just talks to the Google Custom Search API and turns JSON results into a list of URLs — this is only used for the initial seed list, not for anything found while crawling.

**Extracteur** does the actual page work: downloads the HTML, pulls out the title and body text, checks if your keyword shows up, extracts every `<a>` link and resolves relative ones to absolute URLs, and checks `robots.txt` (with caching, so it's not refetching that on every single page from the same site) before deciding whether it's allowed to follow a link at all.

**Sauvegarde** takes anything that matched and writes it to disk as a `.txt` file, handling filename collisions by numbering duplicates.

**Fenetre** is just the Swing window — search box, language dropdown, depth slider, progress bar, results table. It listens for updates from Manager and repaints itself; it doesn't know anything about how the crawling actually happens.

##  Project Structure

```
DeepFocusCrawler/
│
├── com/crawl/
│   │
│   ├── vue/
│   │   ├── Main.java                 # Application entry point
│   │   └── Fenetre.java              # Main GUI window
│   │
│   ├── manager/
│   │   ├── Manager.java              # Central crawler coordinator
│   │   ├── Langue.java               # Language code mapper
│   │   └── languages                 # Language configuration file
│   │
│   ├── downloader/
│   │   ├── Extracteur.java           # HTML parser & link extractor
│   │   ├── Sauvegarde.java           # Content persistence handler
│   │   └── Noeud.java                # Data structure for queue nodes
│   │
│   ├── interfaces/
│   │   └── GoogleRecherche.java      # Google Custom Search API wrapper
│   │
│   └── observer/
│       ├── Observer.java             # Observer interface
│       └── Observable.java           # Observable interface
│
├── README.md                          # This file
├── LICENSE                            # MIT License
├── .gitignore                         # Git ignore rules
└── libraries/
    └── htmlparser.jar                # HTML parsing library
```
```

You'll need a Google Custom Search API key. Type a keyword, pick a language and a depth (1 = seed pages only, 2-3 is usually plenty), pick a folder, hit search.
```

## Before you point this at real sites

The crawler checks `robots.txt`, but it does **not** throttle requests between pages — that's on you to add if you're running this against anything other than a couple of test pages. At minimum:

```java
Thread.sleep(2000 + new Random().nextInt(3000)); // 2–5s between requests
conn.setRequestProperty("User-Agent", "DeepFocusCrawler/1.0 (+https://github.com/khaledkadri/DeepFocusCrawler)");
```

And obviously: respect ToS, copyright, and robots.txt disallows. This was written for learning/research, not for scraping production sites at scale.

## License

MIT — see [LICENSE](LICENSE). Uses `htmlparser` (LGPL).


