# thoth

A ClojureScript frontend to the [route-ccrs-web]() API for the purposes
of performing interactive, exploratory, due date quoting.

## Usage

FIXME

## App Structure Notes

##### How interactive is interactive?

Ideally **realtime**. User clicks on a component, changes something,
the whole quote gets refreshed and updated in around a tenth of a second
(100ms, or less!)

There are two issues with that though:

1. Actually making things happen that quickly (for the _majority_ of
   all interactions.)
2. Communicating the change visually. One change could result in a
   significant visual reorganization of the data; the viewable area
   changing completely, not just the bar clicked on growing smaller.
   _Transitioning_ the display in a non-jarring way could be difficult
   but you can't just suddenly change things out from under the user.

One way to overcome both those pitfalls would be to do things in
_semi_-realtime. By which I mean that the goal is to present the user
with the _current_ state, allow them to edit it, and present information
regarding the results of those edits _in realtime_ but _without_
changing the state they see and are interacting with.

The flow is something like:

1. Get initial due date data.
2. Present that data graphically.
3. User changes the data (overrides a leadtime, etc.)
   1. New due date requested from server.
   2. New due date communicated to user _without_ transitioning the
      graphical quote being viewed.
   3. User can optionally transition to new state based on results.

Some unknowns/potential drawbacks with this approach:

* How do you highlight what has changed, without actually changing it,
  as the user works on the quote?
* Are we effectively just making the user push a "refresh" button after
  every change?

(With this _semi_-realtime approach the user is always working with a
mutable copy of the last agreed upon quote data but the application only
ever stores the immutable, committed, states of the quote. This seems to
fit the model of ClojureScript/Om <-> D3 interaction quite nicely where
you (potentially) don't want every little change to cause a re-render of
the D3 visualization and instead want it to be slightly more mutable.)

## License

Copyright © 2015 Lymington Precision Engineers Co. Ltd.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
