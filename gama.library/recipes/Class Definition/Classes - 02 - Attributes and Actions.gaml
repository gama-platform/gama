/**
* Name: Attributes and Actions
* Author: Alexis Drogoul
* Description:
*   Second in a series of short focused models exploring the class/object
*   additions to GAML introduced in GAMA 2026.
*
*   This model concentrates on working with attributes and actions (methods):
*     1. Reading and writing attributes via the dot operator.
*     2. Calling actions (typed and untyped) on objects.
*     3. Actions that modify the state of their object (mutations).
*     4. Actions that return computed values.
*     5. Chained action calls and action results used as operands.
*     6. Passing objects as arguments to actions.
*     7. Comparing object values.
*
*   Key take-aways:
*     • obj.attr          reads the attribute.
*     • obj.attr <- value writes the attribute.
*     • obj.action(args)  calls the action and yields its return value (if typed).
*     • Objects are NOT agents: they have no reflex, no schedule, no population.
*       All state changes must be driven by explicit calls from the outside.
*
* Tags: class, object, attributes, actions, methods, dot-notation, mutation, GAML
*/
model AttributesAndActions

// ─────────────────────────────────────────────────────────────────────────────
// 1. A mutable counter class
//    Illustrates: attribute read/write, stateful actions, integer return.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A simple integer counter that can be incremented, decremented, reset
 * and queried.  Every state change is performed through an action so that
 * callers never need to manipulate the private {@code value} attribute directly.
 *
 * Attributes:
 * <ul>
 *   <li>{@code label} – a name for this counter, defaulting to {@code "counter"}.</li>
 *   <li>{@code value} – the current integer count, defaulting to 0.</li>
 *   <li>{@code step}  – increment/decrement step size, defaulting to 1.</li>
 * </ul>
 */
class counter {

	/** Human-readable name for this counter. */
	string label <- "counter";

	/** Current count.  Modified by {@code increment}, {@code decrement} and {@code reset}. */
	int value <- 0;

	/** How much to add (or subtract) on each {@code increment}/{@code decrement} call. */
	int step <- 1;

	/**
	 * Adds {@code step} to {@code value} and returns the new value.
	 *
	 * @return the updated count
	 */
	int increment() {
		value <- value + step;
		return value;
	}

	/**
	 * Subtracts {@code step} from {@code value} and returns the new value.
	 *
	 * @return the updated count
	 */
	int decrement() {
		value <- value - step;
		return value;
	}

	/**
	 * Sets {@code value} to 0 and returns the reset value (always 0).
	 *
	 * @return 0
	 */
	int reset() {
		value <- 0;
		return value;
	}

	/**
	 * Returns {@code true} if this counter has the same current value as
	 * another counter {@code other}.
	 *
	 * @param other  the counter to compare against
	 * @return true when both counters hold the same value
	 */
	bool same_value_as(counter other) {
		return value = other.value;
	}

	/**
	 * Returns a human-readable one-line description: label and current value.
	 *
	 * @return descriptive string
	 */
	string to_string() {
		return label + " = " + value + "  (step=" + step + ")";
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. A bank-account class
//    Illustrates: float attributes, conditional logic inside actions,
//                 actions returning bool (success/failure).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A minimal bank-account value object with a current balance, a minimum
 * allowed balance, and deposit / withdraw / transfer operations.
 *
 * All monetary operations return a {@code bool} indicating whether they
 * succeeded (e.g., a withdrawal fails when it would push the balance below
 * {@code min_balance}).
 *
 * Attributes:
 * <ul>
 *   <li>{@code owner}       – the account holder's name.</li>
 *   <li>{@code balance}     – current balance in arbitrary currency units, defaulting to 0.0.</li>
 *   <li>{@code min_balance} – minimum allowed balance (overdraft limit), defaulting to 0.0.</li>
 * </ul>
 */
class bank_account {

	/** Name of the account holder. */
	string owner <- "unknown";

	/** Current balance.  May not fall below {@code min_balance}. */
	float balance <- 0.0;

	/** The minimum balance this account may reach (overdraft floor). */
	float min_balance <- 0.0;

	/**
	 * Deposits {@code amount} into the account.
	 * The deposit is rejected if {@code amount} is not positive.
	 *
	 * @param amount  the amount to deposit (must be > 0)
	 * @return true if the deposit was accepted, false otherwise
	 */
	bool deposit(float amount) {
		if amount <= 0.0 { return false; }
		balance <- balance + amount;
		return true;
	}

	/**
	 * Withdraws {@code amount} from the account.
	 * The withdrawal is rejected if {@code amount} is not positive or if it
	 * would push the balance below {@code min_balance}.
	 *
	 * @param amount  the amount to withdraw (must be > 0 and balance − amount ≥ min_balance)
	 * @return true if the withdrawal succeeded, false otherwise
	 */
	bool withdraw(float amount) {
		if amount <= 0.0                        { return false; }
		if balance - amount < min_balance       { return false; }
		balance <- balance - amount;
		return true;
	}

	/**
	 * Transfers {@code amount} from this account to {@code target}.
	 * First withdraws from this account; if that succeeds, deposits into
	 * {@code target}.  If the withdrawal fails, nothing changes.
	 *
	 * @param amount  the amount to transfer
	 * @param target  the recipient {@code bank_account}
	 * @return true if the transfer completed in full, false if it was rejected
	 */
	bool transfer(float amount, bank_account target) {
		if withdraw(amount) {
			bool ok <- target.deposit(amount);
			return ok;
		}
		return false;
	}

	/**
	 * Returns a formatted one-line summary of the account.
	 *
	 * @return descriptive string
	 */
	string to_string() {
		return owner + ": balance=" + (balance with_precision 2)
		       + "  min=" + (min_balance with_precision 2);
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Global — exercises attribute manipulation and action calls
// ─────────────────────────────────────────────────────────────────────────────

global {

	init {

		// ── 3a. Reading and writing attributes directly ─────────────────────

		write "══ 1. Direct attribute read / write ════════════════════════";

		counter c <- counter(label: "clicks", value: 0, step: 1);
		write "initial  : " + c.to_string();   // clicks = 0

		c.value <- 42;                           // direct write
		write "after direct write (value <- 42): " + c.to_string();

		c.step <- 5;                             // change step size
		write "after step <- 5 : " + c.to_string();

		// ── 3b. Stateful action calls ───────────────────────────────────────

		write "══ 2. Stateful action calls (increment / decrement) ════════";

		int v1 <- c.increment();
		write "after increment(): " + c.to_string() + "  (returned " + v1 + ")";

		int v2 <- c.increment();
		write "after increment(): " + c.to_string() + "  (returned " + v2 + ")";

		int v3 <- c.decrement();
		write "after decrement(): " + c.to_string() + "  (returned " + v3 + ")";

		// Chaining: use the return value directly as an operand
		bool big <- c.increment() > 50;
		write "increment() > 50? " + big + "  counter now: " + c.to_string();

		// ── 3c. Reset and comparison ────────────────────────────────────────

		write "══ 3. Reset and object-to-object comparison ════════════════";

		counter d <- counter(label: "d", value: 0, step: 1);
		write "d          : " + d.to_string();
		write "c same_value_as d? " + c.same_value_as(d);   // false

		c.reset();
		write "c after reset(): " + c.to_string();
		write "c same_value_as d? " + c.same_value_as(d);   // true

		// ── 3d. Bank-account: conditional logic in actions ──────────────────

		write "══ 4. Bank account — actions with conditional logic ════════";

		bank_account alice <- bank_account(owner: "Alice", balance: 500.0, min_balance: 0.0);
		bank_account bob   <- bank_account(owner: "Bob",   balance:  50.0, min_balance: -100.0);

		write "initial  alice: " + alice.to_string();
		write "initial  bob  : " + bob.to_string();

		// Successful deposit
		bool ok1 <- alice.deposit(200.0);
		write "alice.deposit(200) → " + ok1 + "  | " + alice.to_string();

		// Successful withdrawal
		bool ok2 <- alice.withdraw(100.0);
		write "alice.withdraw(100) → " + ok2 + " | " + alice.to_string();

		// Rejected withdrawal (would go below min_balance)
		bool ok3 <- bob.withdraw(200.0);
		write "bob.withdraw(200) → " + ok3 + "  | " + bob.to_string();

		// Transfer
		bool ok4 <- alice.transfer(300.0, bob);
		write "alice.transfer(300 → bob) → " + ok4;
		write "after transfer alice: " + alice.to_string();
		write "after transfer bob  : " + bob.to_string();

		// Rejected transfer (insufficient funds)
		bool ok5 <- alice.transfer(999.0, bob);
		write "alice.transfer(999 → bob) → " + ok5 + "  [rejected]";
		write "alice unchanged: " + alice.to_string();

		// ── 3e. Objects in collections, aggregate operations ────────────────

		write "══ 5. Aggregate operations over a list of objects ══════════";

		list<counter> counters <- [];
		loop i from: 1 to: 5 {
			counters <+ counter(label: "c" + i, value: rnd(10), step: 1);
		}
		write "Counters: " + (counters collect each.to_string());

		int max_val <- max(counters collect each.value);
		write "Max value: " + max_val;

		float mean_val <- mean(counters collect each.value);
		write "Mean value: " + (mean_val with_precision 2);

		// Increment every counter that is below the mean
		loop cnt over: counters {
			if float(cnt.value) < mean_val {
			 	cnt.increment();
			}
		}
		write "After boosting below-mean counters: " + (counters collect each.to_string());
	}
}

// ─────────────────────────────────────────────────────────────────────────────
// Experiment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Minimal GUI experiment — all output is written to the console.
 * Run it and observe the Console view.
 */
experiment "Attributes and Actions" type: gui {
	output {
		// No display needed; all output goes to the console via write
	}
}
