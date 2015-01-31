# lein-packer

A Leiningen plugin to pack file resources.

## How to use

1. put `[lein-packer "0.1.0"]` into the `:plugins` vector of your project.clj.
2. add :pack section into your project.clj, like the following:

```clojure
                   :pack {:mapping [{:source-paths ["manifest.json"
                                                    "resources/public"]
                                     :target-path "target/packed"
                                     :excludes [#"\w+\.\w+\~"]
                                     }]
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

## License

Copyright © 2015 南山

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
