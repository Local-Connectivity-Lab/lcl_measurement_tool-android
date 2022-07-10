# Functionality

`Functionality` directory contains the code for iperf and ping test. 

The test functionalities are wrapped in the `NetworkTestViewModel` class. For each test, we have a dedicated worker that will be selected and dispatched when the test starts.
Under the hood, a worker manager is used to manage all works in the background.