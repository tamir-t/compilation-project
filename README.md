# Compilation project - Arazim team  ![Year](https://img.shields.io/badge/year-2018--19-red.svg) ![CI status](https://img.shields.io/badge/build-passing-brightgreen.svg)

This is the source of our compilation project. WORK IN PROGRESS!

### Requirements
* Linux
* Make, Jflex, probably other libraries

## Running

```
$ cd EX2
$ make
$ make test
$ java -jar PARSER ${input_file} ${output_file}
```

## TODO
- [ ] Check for declarations if they were already declared and abort if indeed.
- [ ] Implement var declaration check for whether or not they are needed to be constant
- [ ] Implemented all helper methods on semantic table
- [ ] Run all tests

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate. Also, we use [Gitmoji](https://gitmoji.carloscuesta.me/) in commit messages.

## License
[MIT](https://choosealicense.com/licenses/mit/)
