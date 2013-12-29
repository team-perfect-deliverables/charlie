##Charlie Project
Charlie, named after the winning hand, five-card charlie, is a
distributed Blackjack system for teaching purposes.

Games, of course, are
powerful tools for teaching; in fact, when playing games or in this case developing them,
the line between learning and having fun is frequently blurred. Are they learning or playing?
We let others decide.

###What does Charlie teach?

* How to look like you know what you're doing should you find yourself stuck in Vegas
* Game theory
* How to make calculated bets
* Probability theory, statistics
* Threads, threadless concurrency, synchronization, communication
* Hyperthreading concepts
* Network programming, serialization
* User interface design
* Artificial intelligence
* Object-oriented principles
* Animation
* Version control
* Server design
* Software testing
* Diagnostic logging
* Even markdown (in the case of this document)

The rest of this document describes the Charlie organization, suggestions for extending it, and known bugs.
I will not described how to play Blackjack or its many variations.
Readers that find lots of Blackjack info in books and/or the Internet.

###A brief history of Blackjack
Blackjack is an example of an excellent game design: it is easy to play yet difficult to master.
It roots go back to Don Quixote and Seville and the 17th century.
Serious study of Blackjack began in the 1950s with long-running computer simulations to discover what is
called the _Basic Strategy_, about which we shall have much to say. By the 1960s, E.O. Thorp, the famous
MIT professor, published, _Beat the Dealer_, which for a while caused casinos to change the game design
to counteract the methods described therein. However, the casino tactics slowed down the game play and customers
stopped coming. It forced casinos to go back to simpler rules and look for other means to thwart
the Basic Strategy and card counting. Peter Griffin published _The Theory of Blackjack_ in the late 1990s. It laid
out what some might regard as the definitive mathematical treatment of Blackjack and player opportunities.
In 2003, Ben Mezrich, _Bringing Down the House_, published a true story of the exploits of students
and their MIT professor in Las Vegas. The story was dramatized by the popular
2008 movie, _21_.
In my own case, I studied Blackjack indepth, asking whether a machine could learn to play
and count cards. The published papers are [here](http://foxweb.marist.edu/users/ron.coleman/).
In 2010, I developed a related
system in (Scala)[www.scala-lang.org]. However, having to teach Scala, too,
in addition to teaching some Blackjack, proved to be too much of a distraction. Thus, the 
goal here is to focus more on Blackjack and Java.

###Charlie structure
Charlie runs on a network with a server and one or more clients.
The login thread, house actor, and dealer objects are passive and run on the server.
By "passive" I mean, they execute only in response to some request.
The GUI runs remotely on the client and in this case the request is directly from the user.

The server flow is login -> house -> dealer.

####Login thread
The login thread listens on a server socket and on accepting a connection, receives a *Login* object
from a user. The login thread authenticates the logname and password and if successful return
a *Ticket* object over the connection.
The *Ticket* contains three key data:

1. Number. This is a random long integer.
2. Bankroll. This is the player escrow funds. The game deducts bets from and deposits earning to this account.
3. House address. This is the actor address of the house which the courier (see below) uses to 
commensumate arrival of the user for play.


