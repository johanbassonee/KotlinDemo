# Kotlin Demo Project

This project contains examples of various Kotlin language features and concepts. It's designed to help you learn Kotlin through practical examples.

## Concepts Covered

### Constructors

Kotlin provides a concise way to define constructors and initialize properties:

- **Primary Constructors**: Defined in the class header
- **Init Blocks**: For initialization logic
- **Secondary Constructors**: Must call the primary constructor
- **Default Parameter Values**: Simplify constructor overloading
- **Property Initialization**: Using constructor parameters

Example: [Constructors.kt](src/main/kotlin/za/co/ee/constructors/Constructors.kt)

### Getters and Setters

Kotlin automatically generates getters and setters for properties, but you can customize them:

- **Custom Getters**: Modify how property values are retrieved
- **Custom Setters**: Modify how property values are set
- **Backing Field**: Access the field value using the `field` identifier

Example: [GetterSetter.kt](src/main/kotlin/za/co/ee/getset/GetterSetter.kt)

### Data Classes

Data classes are designed to hold data with minimal boilerplate:

- **Automatic Generation**: equals(), hashCode(), toString(), copy(), and componentN() functions
- **Immutable vs Mutable**: Using `val` vs `var` properties
- **Copy Function**: Create modified copies of immutable objects
- **Destructuring Declarations**: Extract properties into variables

Example: [DataClasses.kt](src/main/kotlin/za/co/ee/dataclasses/DataClasses.kt)

### Extension Functions

Extend existing classes without inheritance:

- **Custom Extensions**: Add functionality to any class
- **Nullable Receivers**: Extensions that can be called on null values
- **Built-in Extensions**: Kotlin's standard library includes many useful extensions

Examples: 
- [Extentions.kt](src/main/kotlin/za/co/ee/ext/Extentions.kt)
- [MainExt.kt](src/main/kotlin/za/co/ee/ext/MainExt.kt)

### Null Safety

Kotlin's type system helps prevent null pointer exceptions:

- **Nullable Types**: Marked with `?` suffix
- **Safe Call Operator**: `?.` to safely access properties and methods
- **Elvis Operator**: `?:` to provide default values
- **Non-null Assertion**: `!!` to assert a value is not null (use with caution)

Example: [NullableTypeOperator.kt](src/main/kotlin/za/co/ee/nulltypeop/NullableTypeOperator.kt)

### Scope Functions

Execute blocks of code in the context of an object:

- **let**: Uses `it` as context object, returns lambda result
- **apply**: Uses `this` as context object, returns the object itself
- **run**: Uses `this` as context object, returns lambda result
- **with**: Non-extension function that takes an object as parameter
- **also**: Uses `it` as context object, returns the object itself

Example: [ScopeFunctions.kt](src/main/kotlin/za/co/ee/scopefunctions/ScopeFunctions.kt)

### Lambda Expressions

Lambda expressions are anonymous functions that can be passed as arguments or stored in variables:

- **Lambda as a Variable**: Define and store functions in variables
- **Lambda as a Function Argument**: Pass functions as parameters to other functions
- **Trailing Lambda Syntax**: When a lambda is the last parameter, it can be placed outside the parentheses

Example: [Lambda.kt](src/main/kotlin/za/co/ee/lambda/Lambda.kt)

### Objects

Kotlin provides several ways to create and use objects without traditional class instantiation:

- **Singleton Objects**: Create single-instance objects using the `object` keyword
- **Companion Objects**: Add static-like functionality to classes
- **Object Expressions**: Create anonymous objects on the fly
- **Interface Implementation**: Anonymous objects can implement interfaces

Example: [Objects.kt](src/main/kotlin/za/co/ee/objects/Objects.kt)

### Coroutines

Coroutines are Kotlin's solution for asynchronous programming, allowing for more readable and maintainable code:

- **Suspend Functions**: Functions that can be paused and resumed later
- **Coroutine Scope**: Defines the lifecycle of coroutines
- **Dispatchers**: Control which thread the coroutine runs on
- **Structured Concurrency**: Parent-child relationship between coroutines
- **Non-blocking Code**: Write sequential code that doesn't block the main thread

Example: [Coroutines.kt](src/main/kotlin/za/co/ee/coroutine/Coroutines.kt)

## Learning Path

Additional Guide is available here: [Kotlin Leaning Guide](https://docs.google.com/document/d/1ehg-qtKlG1DXG8IcruxkSEcal1QfUXcXu8bidIQh5t4/edit?tab=t.0)

For beginners to Kotlin, we recommend exploring the examples in this order:

1. Constructors and Properties
2. Getters and Setters
3. Null Safety
4. Data Classes
5. Extension Functions
6. Scope Functions
7. Lambda Expressions
8. Objects
9. Coroutines

Each example file contains comments explaining the concepts and how they work.

## Quarkus REST Submodule

This project includes a submodule that demonstrates how to build a REST API using Kotlin and Quarkus:

- **Kotlin with Quarkus**: Build a modern, reactive REST API with Kotlin and Quarkus
- **REST Controller**: Define REST endpoints using JAX-RS annotations
- **Dependency Injection**: Use CDI for service and component management
- **Testing**: Unit tests, integration tests, and WireMock for external service stubbing

The submodule provides a REST API for managing Kotlin learning resources. See the [quarkus-rest README](quarkus-rest/README.md) for more details.
