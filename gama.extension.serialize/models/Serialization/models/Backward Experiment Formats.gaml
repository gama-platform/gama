/**
* Name: Backward Experiment Formats
* Author: Patrick Taillandier, Benoit Gaudou, Alexis Drogoul
* Description: Demonstrates the 'record' experiment type which enables stepping a simulation both forward
*   and backward in time. Built on top of the weighted road-network Base Model, it shows how to declare
*   a recording experiment, how the simulation state is checkpointed each cycle, and how the 'back' and
*   'forward' buttons let the user navigate the recorded history. Useful for interactive debugging and for
*   verifying model reversibility.
* Tags: serialization, record, backward, experiment, replay, road_network, graph, checkpoint
*/

model BackwardExperiments

import "Base Model.gaml"

/**
 * This is the fastest and smallest format for recording simulations. Compression can be enabled to lower the memory usage.
 */
experiment "Binary back and forth" record: true parent: Base;


