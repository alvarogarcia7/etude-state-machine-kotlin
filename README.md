

## Development notes

### About types

This is the case we are talking about:

  * transfer request: amount, description
  * incoming transfer id
  * outgoing transfer id

Note: this code is just an étude. The amount is an integer (no decimals are allowed), and the IDs are just strings, for convenience. On production code, these simplifications are not valid.

#### Solution: Dynamically-typed language

To overcome the step-based data accumulation, in a dynamically-typed language (such as clojure), we would create a map with all the information:

```lisp
{
  :incoming-transfer-id "1234",
  :outgoing-transfer-id "2345",
  :request {
    :amount 10,
    :description "pay for this"
  }
}
```

but this 

#### Solution: Object oriented (using null)

in an object-oriented world, we could create some objects to represent it

```kotlin
data class TransferRequest(val incomingTransferId: String?, val request: Request)
data class Request(val amount: Int, val description: String)
```

The downside of this is that the attributes are nullable (notice the `?` when defining the attribute)

#### Solution: Object oriented (not using null)

Removing the null could be fixed by making the attributes optional:

```kotlin
data class TransferRequest(val incomingTransferId: Option<String>, val request: Option<Request>)
data class Request(val amount: Int, val description: String)
```

but the fact of dealing with the absence or presence of the value is still there.

#### Solution: Object oriented (type-enforced contract)

Another solution can be to get help from the types to enforce the contract:

```kotlin
data class CompleteTransferRequest(
  val incomingTransferId: String,
  val outgoingTransferId: String, 
  val request: Request)
data class IncomingTransferRequest(
  val incomingTransferId: String,
  val request: Request)
data class Request(val amount: Int, val description: String)
```

but then the attribute(s) from the `IncomingTransferRequest` have to be copied to the `CompleteTransferRequest`. This breaks the Open-Closed Principle (the smell is the Shotgun Surgery) in the sense that modifying the `IncomingTransferRequest` also implies modification to the `CompleteTransferRequest`.

#### Solution: Object oriented (type-enforced contract + inheritance)

This could be solved using inheritance:

```kotlin
//Warning: this code does not compile
data class CompleteTransferRequest: IncomingTransferRequest (
  val incomingTransferId: String)
data class IncomingTransferRequest(
  val incomingTransferId: String,
  val request: Request)
data class Request(val amount: Int, val description: String)
```

but unfortunately Kotlin does not allow inheritance for data classes: [here](https://stackoverflow.com/questions/26444145/extend-data-class-in-kotlin), [here](https://stackoverflow.com/questions/44266602/why-kotlin-modifier-open-is-incompatible-with-data)

#### Solution: Object oriented (type-enforced contract + decoration)

or just solve it with decoration

```kotlin
  data class CompleteTransferRequest(val outgoingTransferId: String, val incomingTransferRequest: IncomingTransferRequest)
  data class IncomingTransferRequest(val incomingTransferId: String, val transferRequest: TransferRequest)
  data class Request(val amount: Int, val description: String)
```

This has the problem of the Demeter's Law: to access the description (an inner field of Request) from the outer class you need to navigate:

```kotlin
val completeTransferRequest: CompleteTransferRequest = ...
println("The description is: " + completeTransferRequest.incomingTransferRequest.transferRequest.description)
```

#### Solution: Object oriented (type-enforced contract + decoration + respect Demeter's Law)

Which could be solved by adding methods to the outer layers:

```kotlin
  data class CompleteTransferRequest(val outgoingTransferId: String, val incomingTransferRequest: IncomingTransferRequest){
    fun request(): Request {
      return incomingTransferRequest.request()
    }
    fun incomingTransferId(): String {
      return incomingTransferRequest.incomingTransferId
    }
  }
```

so the navigation is simplified:

```kotlin
val completeTransferRequest: CompleteTransferRequest = ...
println("The description is: " + completeTransferRequest.request().description)
```

#### Solution: Chosen solution: Object oriented (type-enforced contract + decoration)

```kotlin
  data class CompleteTransferRequest(val outgoingTransferId: String, val incomingTransferRequest: IncomingTransferRequest)
  data class IncomingTransferRequest(val incomingTransferId: String, val transferRequest: TransferRequest)
  data class Request(val amount: Int, val description: String)
```

This breaks the Demeter Law but doesn't involve manual work to create accessors to the inner fields.

Some refactorings with the IDE is easy (e.g., rename), but changing the distribution of the fields inside the objects (i.e., move) not so much.


#### Conclusion

The type system, in this case, is beneficial because allows for the compiler to enforce the business restrictions that we need.

On the other hand, in a dynamically-typed language it would be more idiomatic to have everything together. Maybe, even let it crash when the expected data does not match the obtained data.
