##Project Charlie
Everyone probably knows about the famous winning hand, Blackjack, Ace+10.
Then, there's the less famous, less probable but more profitable
five-card Charlie which is a hand of five cards that does not break.

Name after that winning hand,
Project Charlie is an extensible, actor-based Blackjack system for teaching purposes.
Charlie is extensible in the sense that parts of it are built on plug-in modules that can be programmed
without having to rebuild the entire system. Charlie uses [actors](http://en.wikipedia.org/wiki/Actor_model)
as its model of distributed computing. In other words, Charlie is a client-server system
that runs on multiple hosts and can exploit hyper-threaded, multicore processors.

Games are
powerful tools in their own right for teaching; in fact, when playing games or in this case developing them,
blurs the line between learning and playing. Are we learning or playing?
Let others decide.

Blackjack is an example of an excellent game design.
Namely, it is easy to play yet difficult to master.

###What does Charlie teach?

* History
* Game theory
* Utility theory and risk taking
* Probability theory, statistics
* Concurrency and threads
* Hyper-threading concepts
* Synchronization and communication
* Actors
* Network programming
* Serialization
* User interface design
* Artificial intelligence
* Object-oriented principles
* Animation
* Version control
* Server design
* Software testing
* Diagnostic logging
* Even markdown (in the case of this document)
* How to look like you know what you're doing should you get stuck in Vegas

The rest of this document describes the Charlie design and how to extend it.
I will not describe in detail the rules Blackjack.
Readers that find lots of Blackjack info in books and/or the Internet.
I will mention, however, not only _how_ to play Blackjack but also how to make
bets.

###A brief history of Blackjack
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
system in [Scala](www.scala-lang.org). However, having to teach Scala, too,
in addition to teaching some Blackjack, proved to be too much of a distraction. Thus, the 
goal here is to focus more on Blackjack and Java.

###Charlie structure
Charlie uses the model view controller (MVC) design pattern.
The model is roughly represented by the *House* class. It manages login and the bankroll.
The controller is represented by the *Dealer* class. It executes the play rules.
The view is represented by the *IPlayer* interface. There are two concrete IPlayer
classes: *NetPlayer* and *BotPlayer*.
NetPlayer is a server-side [actor](http://en.wikipedia.org/wiki/Actor_model) which
communicates with *Courier*, a client-side actor. NetPlayer and Courier main tasks are to convey
messages between the Dealer and the remote player GUI, the *GameFrame* class.  
BotPlayer, which is not fully implemented,
is designed to run as a bot player using some form of Basic Strategy and card counting system.
