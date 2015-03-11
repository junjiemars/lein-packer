# lein-packer

A Leiningen plugin to pack file resources.

## As a Plugin

1. put `[lein-packer "0.1.0"]` into the `:plugins` vector of your project.clj.
2. add :pack section into your project.clj, like the following:

```clojure
                   :pack {:mapping [{:source-paths ["manifest.json"
                                                    "resources/public"]
                                     :target-path "target/packed"
                                     :excludes [#"\w+\.\w+\~"]}]
                          :target {:type "crx"
                                   :path "target/"}}}
```   
3. once task:
```shell
lein packer once
```
4. clean task: 
```shell
lein packer clean
```

## As a Hook
add following section into your project.clj file
```clojure
:hooks [lein-packer]
```

## As a Notify Command
If you want to write a Chrome's extension in clojurescript, so it's awesome case to pack your artifacts into the output directory after you compiled your clojurescript code.
insert into the following code into your project.clj file.
```clojure
                   :cljsbuild {:builds
                               [{:source-paths ["src/brepl" "src/cljs"]
                                 :compiler {:externs
                                            ["externs/chrome_extensions.js"]
                                            :output-to
                                            "resources/public/js/main.js"
                                            :optimizations :whitespace
                                            :pretty-print true}
                                 :notify-command ["lein" "packer" "once"]
                                 }]}
```

## How to use
```source-paths``` used to specify what to pack;
```target-path``` used to specify pack to where;
```excludes``` used to exclude every thing you don't want in normal regex patterns.

## License

Copyright © 2015 南山

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
