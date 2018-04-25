dlcrypto_encode
===============

A crypto hash safe encoder for Scala value objects

The problem
-----------
When writing applications tha rely on cryptographic hashing and signing, you
need to be sure that the Hash of your document *uniquely* came from that
document. If an adversary can construct a different document from yours but 
with the same Hash, then the adversary can make it look like you signed his
document rather than your own!

If your document is just a string, then ensuring the Hash is unique to your
string is easy. But what if your document is actually a structured object?
The document needs to be encoded in some way so that the Hash function can be
applied. However, how to do this?

A naive encoder might simply recursively traverse the document and concatenate
everything but that could create attacks when

```Encode(Seq("experts","exchange")) == Encode(Seq("expert","sex","change))```

And the resultant hash of these two documents ends up being the same!

Why this library?
-----------------
We want to be able to define vanilla Scala case classes like

```case class MyDocument(a:String, b: Int, c:Other)```

And get a cryptographic "safe" Hash from it. 

**What does "safe" mean?**

It means that the encoding (and thus hashing, and this signatures based thereof) 
of two objects will only be the same if the objects themselves are the "same"

**When are two objects the "same"?**

If the two objects equal BUT ignoring any class names

**Why ignore class names?**

For pragmatic reasons, this is simply the most useful implementation. But for a
proof by contradiction, it class names were allowed, what about the package name?
Then what about the classpath? What about the object obtained from the web where
there is neither package nor classpath? What if you want to do the same encoding
in JavaScript too?

How it works?
-------------
The best way to see the specification at a high level is to look at the test cases

In terms of the actual encoding, it is based on the Google Protobuf 2 standard

**Why Google Protobuf?**

After researching so many cross-platform/language serializing libraries, this was
one where the author could convince himself that the serialization scheme matched
the requirements above in terms of safety.

Research included starting with a handcrafted serializer independent of Protobuf 
entirely and evolving its implementation towards protobuf while still ensuring it 
worked and is compatible

**Can any protobuf compatible class be encoded?**

No, the full protobuf serialization spec may not be "safe" for Hashing. For that
reason, the implementation of the library remains the hand-crafted encoder. Of
course, developers can still use protobuf libraries to transmit documents elsewhere
if desired and be reasonably assured of compatibility.

To ensure that the encoding is "safe", only a subset of types can be safely encoded.
Objects must be
- Value objects/DTOs, as a general design principle 
- Immutable (recursively)
- Case classes containing
  - Scalars
  - "safe" value objects
  - Immutable collections of the above
Note that the current library neither promises safety nor does it promise to make all
safe types available for encoding. Thus caveat emptor that you choose types well that are
"safe" for encoding for hashing and signing. Failure to check this will leave you open
to potential attack.

Checking for full safety is possible but not implemented for now. As is extending the 
supported types. Patches welcome

**Why not protobuf3?**

Due to the way protobuf3 encodes structures, it is possible for the "same" document
to be encoded in two different ways. That's unlikely to happen if you use the same
library to encode/decode but in a cross-platform/language scenario where you want to
also perform encoding, if you've encoded to a different byte stream, the hashes will
not match. This is not a problem for serialization (since either library can 
deserialize either form to the same object) but it is a major annoyance for encoding
for cryptographic hashing. 
This problem does not happen if proto2 is used with all required fields, hence the use of
the proto2 binary encoding.
 
**Can I define my classes as .proto files rather than as case classes?**

It is convenient during prototyping to simply create the case classes and have them readily
hashable. But in a full stack application, you may wish to use .proto files as the common
base so they can be reused, for example on the JavaScript side.

Thus you can define proto2 files and have case classes generated from them. This library
has been successfully tested with ScalaPB and test cases will be added to this library
to prove this some time in the future.

Please note that all fields must be `required` and you have to choose your types carefully
per above guidelines