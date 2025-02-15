[![Build Status](https://travis-ci.org/skjolber/3d-bin-container-packing.svg)](https://travis-ci.org/skjolber/3d-bin-container-packing)

# 3d-bin-container-packing

This library does 3D rectangular bin packing; it attempts to match a set of 3D items to one or more in a set of 3D containers. The result can be constrained to a maximum number of containers.

Projects using this library will benefit from:
 * short and predictable calculation time,
 * fairly good use of container space, 
 * brute-force support for low number of boxes (ideal for small orders)
    
Bugs, feature suggestions and help requests can be filed with the [issue-tracker].

## Obtain
The project is implemented in Java and built using [Maven]. The project is available on the central Maven repository.

<details>
  <summary>Maven coordinates</summary>

Add
 
```xml
<3d-bin-container-packing.version>2.1.0</3d-bin-container-packing.version>
```

and

```xml
<dependency>
    <groupId>com.github.skjolber.3d-bin-container-packing</groupId>
    <artifactId>core</artifactId>
    <version>${3d-bin-container-packing.version}</version>
</dependency>
```

</details>

or

<details>
  <summary>Gradle coordinates</summary>

For

```groovy
ext {
  containerBinPackingVersion = '2.1.0'
}
```

add

```groovy
api("com.github.skjolber.3d-bin-container-packing:core:${containerBinPackingVersion}")
```

</details>

Java 11+ projects please use module `com.github.skjolber.packing.core`.

# Usage
The units of measure is out-of-scope, be they cm, mm or inches.

### Largest Area Fit First (LAFF) packager
Obtain a `Packager` instance:

```java
Container container = Container.newBuilder()
    .withDescription("1")
    .withSize(10, 10, 3)
    .withEmptyWeight(1)
    .withMaxLoadWeight(100)
    .build();
    
LargestAreaFitFirstPackager packager = LargestAreaFitFirstPackager.newBuilder()
    .withContainers(Arrays.asList(container))
    .build();
```

The `packager` instance is thread-safe.

### Packing
Then compose your item list and perform packing:

```java
List<StackableItem> products = new ArrayList<StackableItem>();

products.add(new StackableItem(Box.newBuilder().withId("Foot").withSize(6, 10, 2).withRotate3D().withWeight(25).build(), 1));
products.add(new StackableItem(Box.newBuilder().withId("Leg").withSize(4, 10, 1).withRotate3D().withWeight(25).build(), 1));
products.add(new StackableItem(Box.newBuilder().withId("Arm").withSize(4, 10, 2).withRotate3D().withWeight(50).build(), 1));

// match a single container
Container match = packager.pack(products);
```

The resulting `match` variable returning the resulting packaging details or null if no match. 

The above example would return a match (`Foot` and `Arm` would be packaged at the height 0, `Leg` at height 2). 

For matching against multiple containers use

```java
int maxContainers = ...; // maximum number of containers which can be used
long deadline = ...; // system time in milliseconds at which the search should be aborted

// match multiple containers
List<Container> fits = packager.packList(products, maxContainers, deadline);
```

### Brute-force packager
For a low number of packages (like <= 6) the brute force packager might be a good fit. 

```java
Packager packager = BruteForcePackager.newBuilder()
    .withContainers(Arrays.asList(container)
    .build();
```

Using a deadline is recommended whenever brute-forcing in a real-time application.

<details>
  <summary>Algorithm details</summary>
 
### Largest Area Fit First algorithm
The implementation is based on [this paper][2], and is not a traditional [bin packing problem][1] solver.

The box which covers the largest ground area of the container is placed first; its height becomes the level height. Boxes which fill the full remaining height take priority. Subsequent boxes are stacked in the remaining space in at the same level, the boxes with the greatest volume first. If box height is lower than level height, the algorithm attempts to place some there as well. 

When no more boxes fit in a level, the level is incremented and the process repeated. Boxes are rotated, containers not.

 * `LargestAreaFitFirstPackager` stacks in 3D within each level
 * `FastLargestAreaFitFirstPackager` stacks in 2D within each level

The algorithm runs reasonably fast, usually in milliseconds. Some customization is possible.

### Plain algorithm
This algorithm selects the box with the biggest volume, fitting it in the tightest possible point.

###  Brute-force algorithm
This algorithm has no logic for selecting the best box or rotation; running through all permutations, for each permutation all rotations:

 * `BruteForcePackager` attempts all box orders, rotations and placement positions.
 * `FastLargestAreaFitFirstPackager` selects all box orders and rotations, selecting the most appropriate placement position.

The maximum complexity of this approach is [exponential] at __n! * 6^n__ or worse. The algorithm runs for under a second for small number of products (<= 6), to seconds or minutes (<= 8) or hours for larger numbers.

However accounting for container vs box size plus boxes with equal size might reduce this bound considerably, and the resulting complexity can be calculated using a `PermutationRotationIterator` before packaging is attempted.

There is also a parallel version `ParallelBruteForcePackager` of the brute-force packager, for those wishing to use it on a multi-core system.

Using a brute-force algorithm might seem to hit a wall of complexity, but taking into account number of items 
per order distribution for web-shops, a healthy part of the orders are within its grasp.

Note that the algorithm is recursive on the number of boxes, so do not attempt this with many boxes (it will likely not complete in time anyhow).

</details> 
 
### Visualizer
There is a simple output [visualizer](visualization) included in this project, based of [three.js](https://threejs.org/). This visualizer is currently intended as a tool for developing better algorithms (not as stacking instructions).

![Alt text](visualizer/viewer/images/view.png?raw=true "Demo")

# Customization
The code has been structured so it is possible to extend and adapt to specialized needs. See `AbstractPackager` class, the `extreme-points` and `test` artifacts. 

To use the visualizer during development, make your unit tests write directly to a file in the project (see `VisualizationTest` example). 

# Get involved
If you have any questions, comments or improvement suggestions, please file an issue or submit a pull-request.

Feel free to connect with me on [LinkedIn], see also my [Github page].

## License
[Apache 2.0]. Social media preview by [pch.vector on www.freepik.com](https://www.freepik.com/free-photos-vectors/people).

# History
 * 2.1.0: Improve brute force iterators, respect deadlines in brute for packagers.
 * 2.0.10: Fix volume calculation overflow
 * 2.0.9: Better abstractions for comparisons of pack result.
 * 2.0.8: Fix regression, improve testing.
 * 2.0.7: Fix issue #453, improve performance.
 * 2.0.6: Fix issue #450
 * 2.0.5: Fix issue #440 and #433
 * 2.0.4: Performance improvements, minor bug fixes.
 * 2.0.2: Fix bug with multiple containers.
 * 2.0.1: Performance improvements.
 * 2.0.0: Major refactoring and improvements. Note: __New Maven coordinates__

[1]: 				https://en.wikipedia.org/wiki/Bin_packing_problem
[2]: 				https://www.drupal.org/files/An%20Efficient%20Algorithm%20for%203D%20Rectangular%20Box%20Packing.pdf
[Apache 2.0]: 		http://www.apache.org/licenses/LICENSE-2.0.html
[issue-tracker]:	https://github.com/skjolber/3d-bin-container-packing/issues
[Maven]:			http://maven.apache.org/
[LinkedIn]:			http://lnkd.in/r7PWDz
[Github page]:		https://skjolber.github.io
[NothinRandom]:		https://github.com/NothinRandom
[exponential]:		https://en.wikipedia.org/wiki/Exponential_function

