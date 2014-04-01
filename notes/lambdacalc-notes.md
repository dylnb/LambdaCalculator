TODO
====

* Write example files for polymorphism



Platform and Distribution
-------------------------

* Find out about the Mac App Store, or about code-signing to avoid corruption
  messages

* Get the thing working on Mavericks
    + Mavericks file bug: might be due to trying to find appropriate icons



Appearance and Convenience
--------------------------

* Separate out left and right function application options in student version.
  This could help handling situations in which multiple composition rules are
  applicable

* Make the Latex button a toggle switch


Polymorphism
------------

* Figure out something to do when multiple composition rules are applicable.
  This probably requires a general change to the way meanings are computed,
  so that the entire tree isn't pre-computed upon loading an exercise (as it
  stands, all compositor ambiguity must be resolved before the exercise loads)


File Loading Bugs
-----------------

* Keep bare indices from picking up types from traces in different sentences,
  or from previous files


Tree Widget Bugs
----------------

* Evaluate Node (Fully); undo until fully unevaluated; Evaluate Node (Fully)
  again. "Problem: I do not know how to combine the children ..."

* If lexical terminals are manually selected/entered, then mismatched types
  generate "Type Unknown" but don't throw "Problem: I do not know how to
  combine the children ..."


Type Exercise Bugs
------------------

* Clicking on an already-completed exercise pulls the answer up, but not the
  question


Miscellaneous Observations
--------------------------

* Can't explicitly type user-defined lexical entries (like "happy" in Ex3.txt)

* If 'A', 'E', 'I', etc. are assigned as variables with a certain type, the
  exercise fails to load with "Expecting a variable, but variables must start
  with a letter". Presumably this is because 'A', 'E', etc. are reserved logical
  symbols. The error message should be more informative.
