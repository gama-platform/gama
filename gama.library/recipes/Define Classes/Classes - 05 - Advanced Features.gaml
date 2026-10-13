/**
* Name: Advanced Features
* Author: Alexis Drogoul
* Description:
*   Fifth and last in a series of short focused models exploring the class/object
*   additions to GAML introduced in GAMA 2026.
*
*   This model concentrates on the more advanced aspects of the class system:
*     1. Objects as agent attributes — an agent that owns an object (composition).
*     2. Passing objects between agents — one agent sends an object to another.
*     3. Objects as return values of agent actions.
*     4. Using class instances in global variables and in experiment parameters.
*     5. Polymorphic objects in agent collections.
*     6. Object equality and identity.
*     7. Using objects as keys inside maps.
*     8. Objects combined with species: a species whose agents carry objects.
*
*   Key take-aways:
*     • Objects can be stored in any GAML variable: global, species, local, etc.
*     • Objects follow GAML value semantics: assigning to a variable copies
*       the reference (not the object itself).
*     • The equality operator "=" on objects tests reference identity by default
*       (two objects are the same iff they are the exact same instance).
*     • Objects can be used as map keys since they support identity-based hashing.
*     • Agents and objects can freely exchange data through shared references.
*
* Tags: class, object, agents, composition, passing, map, advanced, GAML, OOP
*/
model AdvancedFeatures

// ─────────────────────────────────────────────────────────────────────────────
// 1. Supporting classes
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An immutable 2-D vector used as a lightweight value object.
 * Demonstrates that classes can model purely mathematical data.
 *
 * Attributes:
 * <ul>
 *   <li>{@code dx} – horizontal component, defaulting to 0.0.</li>
 *   <li>{@code dy} – vertical component, defaulting to 0.0.</li>
 * </ul>
 */
class vec2 {

	/** Horizontal component. */
	float dx <- 0.0;

	/** Vertical component. */
	float dy <- 0.0;

	/**
	 * Returns the Euclidean norm (length) of this vector.
	 *
	 * @return the magnitude as a float
	 */
	float norm() {
		return sqrt(dx ^ 2 + dy ^ 2);
	}

	/**
	 * Returns the sum of this vector and {@code other} as a new {@code vec2}.
	 *
	 * @param other  the vector to add
	 * @return a new vec2 representing the component-wise sum
	 */
	vec2 add(vec2 other) {
		return vec2(dx: dx + other.dx, dy: dy + other.dy);
	}

	/**
	 * Returns a scaled copy of this vector.
	 *
	 * @param factor  the scaling factor
	 * @return a new vec2 scaled by factor
	 */
	vec2 scale(float factor) {
		return vec2(dx: dx * factor, dy: dy * factor);
	}

	/**
	 * Returns a human-readable representation "(dx, dy)".
	 *
	 * @return string form of the vector
	 */
	string to_string() {
		return "(" + (dx with_precision 2) + ", " + (dy with_precision 2) + ")";
	}
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * A message object that can be passed from one agent to another. 
 * Again, this contrasts with the 'message' predefined type, which creates immutable messages
 * Demonstrates using objects as data carriers between agents.
 *
 * Attributes:
 * <ul>
 *   <li>{@code sender_name} – name of the sending agent.</li>
 *   <li>{@code content}     – textual content of the message.</li>
 *   <li>{@code priority}    – numeric priority (higher is more urgent), defaulting to 1.</li>
 *   <li>{@code read}        – whether the message has been read, defaulting to {@code false}.</li>
 * </ul>
 */
class message_object {

	/** Name of the agent that created this message. */
	string sender_name <- "unknown";

	/** Text content of the message. */
	string content <- "";

	/** Priority level (higher = more urgent). */
	int priority <- 1;

	/** Whether the message has been read by its recipient. */
	bool read_flag <- false;

	/**
	 * Marks this message as read and returns its content.
	 *
	 * @return the content string
	 */
	string consume() {
		read_flag <- true;
		return content;
	}

	/**
	 * Returns a formatted one-line representation of the message.
	 *
	 * @return descriptive string
	 */
	string to_string() {
		return "[msg p=" + priority + (read_flag ? " ✓" : "  ")
		       + "] from=" + sender_name + " : ("  + content + " ";
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Agent species — node
//    Each agent owns a list of messages and a velocity vector.
//    Agents can send objects to one another, and process received objects.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A node agent that carries a list of {@code message} objects and a
 * {@code vec2} velocity.
 *
 * Demonstrates:
 * <ul>
 *   <li>Agent attributes typed with user-defined classes.</li>
 *   <li>Passing objects from one agent to another via an action argument.</li>
 *   <li>Returning an object from an agent action.</li>
 *   <li>Polymorphic object lists inside agents.</li>
 * </ul>
 *
 * Species attributes:
 * <ul>
 *   <li>{@code inbox}    – the list of messages received by this agent.</li>
 *   <li>{@code velocity} – the current movement vector of this node.</li>
 *   <li>{@code energy}   – abstract energy level, defaulting to 100.0.</li>
 * </ul>
 */
species node_agent skills: [moving] {

	/** Incoming messages queued for this agent. */
	list<message_object> inbox <- [];

	/** Current velocity vector. */
	vec2 velocity <- vec2(dx: rnd(2.0) - 1.0, dy: rnd(2.0) - 1.0);

	/** Abstract energy level. */
	float energy <- 100.0;

	// ── Actions ────────────────────────────────────────────────────────────

	/**
	 * Receives a {@code message} object and places it in {@code inbox}.
	 * Because the parameter is a reference, both sender and receiver share
	 * the same object in memory until either replaces it.
	 *
	 * @param msg  the message object to enqueue
	 */
	action receive_message(message_object msg) {
		inbox <+ msg;
	}

	/**
	 * Processes (reads) all messages in the inbox, consuming each one and
	 * deducting a small energy cost proportional to message priority.
	 * Clears the inbox when done.
	 *
	 * @return the number of messages processed
	 */
	int process_inbox() {
		int count <- length(inbox);
		loop msg over: inbox {
			string text <- msg.consume();
			energy <- energy - msg.priority * 0.5;
		}
		inbox <- [];
		return count;
	}

	/**
	 * Creates and returns a new {@code message} object addressed from this
	 * agent, with the given content and priority.
	 *
	 * Returning an object from an agent action is identical to returning any
	 * other GAML value.
	 *
	 * @param content   the message text
	 * @param priority  the urgency level
	 * @return a new {@code message} object
	 */
	message_object compose(string msg_content, int msg_priority) {
		return message_object(sender_name: name,
		               content: msg_content,
		               priority: msg_priority);
	}

	// ── Reflexes ───────────────────────────────────────────────────────────

	/** Apply velocity to the agent location each cycle. */
	reflex drift {
		location <- {
			(location.x + velocity.dx) mod world.shape.width,
			(location.y + velocity.dy) mod world.shape.height
		};
	}

	/** Slowly process unread messages and lose energy accordingly. */
	reflex check_mail when: !empty(inbox) {
		int n <- process_inbox();
	}

	/** Regain a small amount of energy each cycle. */
	reflex recharge {
		energy <- min(100.0, energy + 0.2);
	}

	// ── Aspect ─────────────────────────────────────────────────────────────

	/**
	 * Draws the node as a circle whose radius reflects its energy level, with
	 * the number of unread messages displayed nearby.
	 */
	aspect default {
		float r <- max(1.0, energy / 20.0);
		draw circle(r) color: rgb(int(energy * 2.55), 100, 50) border: #white;
		if !empty(inbox) {
			draw string(length(inbox)) at: location + {0, -r - 0.5}
			     color: #yellow size: 2.0;
		}
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Global
// ─────────────────────────────────────────────────────────────────────────────

global {

	/** World boundary. */
	geometry shape <- square(100);

	/** Number of nodes to create. */
	int nb_nodes <- 10;

	init {

		create node_agent number: nb_nodes;

		// ── 3a. Objects as return values ──────────────────────────────────────

		write "══ 1. Objects as return values of agent actions ════════════";

		node_agent sender <- first(node_agent);
		message_object m1 <- sender.compose("Hello world", 2);
		write "Created: " + m1.to_string();

		// ── 3b. Passing an object from one agent to another ───────────────────

		write "══ 2. Passing objects between agents ═══════════════════════";

		node_agent recipient <- last(node_agent);
		ask recipient { do receive_message(m1); }
		write recipient.name + " inbox size: " + length(recipient.inbox);
		write "Message in inbox: " + recipient.inbox[0].to_string();

		// Both sender and recipient hold a reference to the SAME object
		write "Same object? " + (m1 = recipient.inbox[0]);   // true: reference equality

		// ── 3c. Vector objects — value created by action, assigned, composed ──

		write "══ 3. vec2 objects: composition via action return values ═══";

		vec2 v1 <- vec2(dx: 3.0, dy: 4.0);
		vec2 v2 <- vec2(dx: 1.0, dy: -2.0);

		vec2 sum_v <- v1.add(v2);
		write "v1 + v2 = " + sum_v.to_string();
		write "|v1| = " + (v1.norm() with_precision 3);

		vec2 scaled <- v1.scale(2.5);
		write "2.5 × v1 = " + scaled.to_string();

		// Chain: add then scale
		vec2 combined <- v1.add(v2).scale(0.5);
		write "0.5 × (v1 + v2) = " + combined.to_string();

		// ── 3d. Reference identity vs. structural equality ────────────────────

		write "══ 4. Object identity (reference equality) ══════════════════";

		vec2 a <- vec2(dx: 3.0, dy: 4.0);
		vec2 b <- vec2(dx: 3.0, dy: 4.0);   // different object, same values
		vec2 c_ref <- a;                     // same reference as a

		write "a = " + a.to_string();
		write "b = " + b.to_string();
		write "a = b (same reference)? " + (a = b);          // false
		write "a = c_ref (same reference)? " + (a = c_ref);  // true

		// ── 3e. Objects as map keys ───────────────────────────────────────────

		write "══ 5. Objects as map keys ══════════════════════════════════";

		map<vec2, string> labels;
		labels[a] <- "origin vector";
		labels[b] <- "copy vector";

		write "labels[a] = " + labels[a];
		write "labels[b] = " + labels[b];
		write "labels[c_ref] (same ref as a) = " + labels[c_ref];   // same as a

		// ── 3f. Polymorphic objects inside agent collections ─────────────────

		write "══ 6. Sending multiple messages with different priorities ═══";

		loop i from: 1 to: 3 {
			node_agent s <- any(node_agent);
			node_agent r <- any(node_agent where (each != s));
			if r != nil {
				message_object msg <- s.compose("ping #" + i, i);
				ask r { do receive_message(msg); }
			}
		}

		write "Inbox sizes after sending:";
		ask node_agent {
			if !empty(inbox) {
				write "  " + name + " → " + length(inbox) + " message(s)";
			}
		}

		// Sort all messages by priority across all agents
		list<message_object> all_msgs <- node_agent accumulate each.inbox;
		list<message_object> sorted <- all_msgs sort_by (-each.priority);
		write "All pending messages sorted by priority (desc):";
		loop msg over: sorted {
			write "  " + msg.to_string();
		}
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// Experiment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GUI experiment.  Displays the node agents and their energy in a 2-D view,
 * while the console shows detailed object-level information.
 */
experiment "Advanced Features" type: gui {

	float minimum_cycle_duration <- 0.05;

	/** Number of nodes — also used as a parameter in the GUI. */
	parameter "Number of nodes" var: nb_nodes min: 2 max: 50 category: "Setup";

	output {
		display "Nodes" type: 2d background: #black {
			species node_agent;
		}
	}
}
