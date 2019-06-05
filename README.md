# (CL)S Solver for Labyrinths

Finds all ways from start (s) to goal (g) in a labyrinth such as

```
|x|g|x|
| | | | 
|s|x| |
| | | | 
```

where x-fields are blocked tiles.
Uses (CL)S to compute the solution.
The example is taken from:

Jan Bessai, Anna Vasileva: User Support for the Combinator Logic Synthesizer Framework. F-IDE@FLoC 2018: 16-25. Online [here](https://doi.org/10.4204/EPTCS.284.2).

Results are served via MQTT.

Anna Vasilieva and Moritz Reudel created a [video demonstration](https://github.com/combinators/labyrinth/blob/master/video.mp4) using the laser based visualization facilities of the test lab at [FLW Dortmund](https://flw.mb.tu-dortmund.de/).
In the demo results are interpreted by Unity3D which then controlls the laser system to show computed movements.

The protocol is as follows:
0. Start an MQTT broker, e.g. [Mosquitto](https://mosquitto.org/) via `mosquitto -c mosquitto.conf` with the [provided config](https://github.com/combinators/labyrinth/blob/master/mosquitto.conf).
1. Start an [Agent](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/Agent.scala) using `sbt run`.
2. Send a JSON-encoded [Task](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L6) to the [broker](https://github.com/combinators/labyrinth/blob/master/src/main/resources/org/combinators/labyrinth/connection.properties#L1) under the [taskTopic](https://github.com/combinators/labyrinth/blob/master/src/main/resources/org/combinators/labyrinth/connection.properties#L3).
3. Send a JSON-encoded [request for solutions](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L15) to the broker under the [topicForRequests](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L8) specified in your task.
4. Receive [maxCount](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L15) JSON-encoded [Solution](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L25) replies under the [topicForSolutions](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/protocol/data.scala#L7) specified in your task.

The agent can be cleanly exited by pressing enter.

Solutions will be ordered by the number of necessary steps and 'wrap around' (be repeated) if more solutions are requested than possible.
Protocol data is encoded and decoded using [circe](https://circe.github.io/circe/).

You can use `sbt run` to start a [Demo](https://github.com/combinators/labyrinth/blob/master/src/main/scala/org/combinators/labyrinth/Demo.scala) client.
To avoid trouble, make sure the agent is running before starting the demo client.

By default the [log level](https://github.com/combinators/labyrinth/blob/master/src/main/resources/logback.xml) is [configured](https://logback.qos.ch/manual/configuration.html) to `DEBUG`. 
This will print all JSON messages exchanged with the agent.

