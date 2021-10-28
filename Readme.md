RunDatabase
Primary Key  -> unique identifier
Bitmap -> Here we are saving the bitmap inside the db but we don't prefered to use so to get the data from the database
we are using the type convertor
1st function -> convert bitmap into format which room understands
2nd function -> convert room understand format in which we can use it
--------------------------------------------
Dao -> Data access Object it's just an interface which describe all the possible action's we had to
do with our database

insert & delete -> should be suspend as we had to do the task in the bg
getAllRunsSortedByDate -> as we just had to observe the data here so that's why it's not in suspend
Order by -> we are sorting the data by time wise
Desc(descending) -> latest run should be on the top of the list

Note :- here we created different - different function so to sort easily.
--------------------------------------------

Dagger -> it's the library for the Dependency Injections
Dependency -> kotlin object or variable
-> if objectA then objectB is a dependency of objectA
Example    -> in the mainActivity we had to deal with viewModel but as viewModel is depend on viewModelFactory
viewModelFactory is depend on Repository and repository is depend on the roomDatabase so if
we had to get access to the viewModel then we had to create the object of all these.
Solution   -> Instead of creating soo much we can simply using Dagger and using dagger the only thing we had to do is
adding the @inject annotation that will tell dagger find the dependency.
-> dagger also provide to scope our dependency which means we can control the lifetime.
