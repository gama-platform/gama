/*******************************************************************************************************
 *
 * StaticBodySkill.java, in gaml.extensions.physics, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.gaml;

import java.util.HashMap;
import java.util.Map;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.listener;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IPhysicalConstants;
import gama.gaml.skills.GamlSkill;
import gama.gaml.types.IType;

/**
 * The Class StaticBodySkill.
 */
@vars ({
		// @variable (
		// name = IKeyword.LOCATION,
		// type = IType.POINT,
		// depends_on = IKeyword.SHAPE,
		// doc = @doc ("Represents the current position of the agent")),
		@variable (
				name = IPhysicalConstants.MASS,
				type = IType.FLOAT,
				init = "1.0",
				doc = { @doc ("The mass of the agent. Should be equal to 0.0 for static, motionless agents") }),
		@variable (
				name = IPhysicalConstants.ROTATION,
				type = IType.PAIR,
				of = IType.POINT,
				index = IType.FLOAT,
				init = "0.0::{0,0,1}",
				doc = { @doc ("The rotation of the physical body, expressed as a pair which key is the angle in degrees and value the axis around which it is measured") }),

		@variable (
				name = IPhysicalConstants.FRICTION,
				type = IType.FLOAT,
				init = "0.5",
				doc = { @doc ("Between 0 and 1. The coefficient of friction of the agent (how much it decelerates the agents in contact with him). Default is 0.5") }),
		@variable (
				name = IPhysicalConstants.RESTITUTION,
				type = IType.FLOAT,
				init = "0.0",
				doc = { @doc ("Between 0 and 1. The coefficient of restitution of the agent (defines the 'bounciness' of the agent). Default is 0") }),

		@variable (
				name = IPhysicalConstants.AABB,
				type = IType.GEOMETRY,
				doc = { @doc ("The axis-aligned bounding box. A box used to evaluate the probability of contacts between objects. Can be displayed as any other GAMA shapes/geometries in order to verify that the physical representation of the agent corresponds to its geometry in the model") }), })

@skill (
		name = IPhysicalConstants.STATIC_BODY,
		concept = { IConcept.SKILL, IConcept.THREED },
		doc = { @doc ("A skill allowing an agent to b a static object in a physical 3D world (if it is also registered in a model inheriting from '"
				+ IPhysicalConstants.PHYSICAL_WORLD + "'). Proposes a number of new attributes ('"
				+ IPhysicalConstants.FRICTION + "', '" + IPhysicalConstants.RESTITUTION
				+ "',...) and the actions called '" + IPhysicalConstants.CONTACT_ADDED + "'/'"
				+ IPhysicalConstants.CONTACT_REMOVED
				+ "' in order for the agent to be informed when it is in physical contact with an other. These actions will be called in turn for each colliding agent. ") })
/**
 * A class that supports the definition of agents provided with static bodies in a physical world. It comes with new
 * variables (friction, restitution, body, field, AABB, etc.) and gives the possibility to all its instances to be
 * notified when contacts occur with other instances of either 'static_body' or 'dynamic_body'
 *
 * @author Alexis Drogoul 2021
 *
 */
public class StaticBodySkill extends GamlSkill implements IPhysicalConstants {

	/**
	 * Gets the body.
	 *
	 * @param agent
	 *            the agent
	 * @return the body
	 */
	protected IBody getBody(final IAgent agent) {
		IBody result = (IBody) agent.getAttribute(BODY);
		// if it is null, the agent is not yet registered in the physical world
		// we create a temporary fake body, that can hold the initialisation of
		// variables, and that will be replaced as
		// soon as the agent is registered
		if (result == null) {
			result = new FakeBody();
			agent.setAttribute(BODY, result);
		}
		return result;
	}

	/**
	 * Static bodies have no mass in Bullet
	 */
	@getter (
			value = MASS,
			initializer = true)
	public Double getMass(final IAgent scope) {
		return 0d;
	}

	/**
	 * We prevent modelers from providing a mass
	 */
	@setter (MASS)
	public void setMass(final IAgent a, final Double value) {}

	/**
	 * Listens to the change in the location of the agent (whether these changes come from the model in GAML or from
	 * another plugin/skill in Java) in order to synchronize the location in GAMA with the location in Bullet
	 *
	 */
	@listener (IKeyword.LOCATION)
	public void changeInLocation(final IAgent a, final GamaPoint loc) {
		IBody body = getBody(a);
		if (body == null) return;
		body.setLocation(loc);
	}

	/**
	 * Change in heading.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param a
	 *            the a
	 * @param loc
	 *            the loc
	 * @date 4 oct. 2023
	 */
	@listener (IKeyword.HEADING)
	public void changeInHeading(final IAgent a, final Double heading) {
		IBody body = getBody(a);
		if (body == null) return;
		GamaPoint p = new GamaPoint();
		body.getLinearVelocity(p);
		double rad = Math.toRadians(heading);
		double speed = p.norm();
		p.setLocation(speed * Math.cos(rad), speed * Math.sin(rad), 0);
		body.setLinearVelocity(p);
	}

	/**
	 * Change in speed.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param a
	 *            the a
	 * @param loc
	 *            the loc
	 * @date 4 oct. 2023
	 */
	@listener (IKeyword.SPEED)
	public void changeInSpeed(final IAgent a, final Double speed) {
		IBody body = getBody(a);
		if (body == null) return;
		if (speed <= 0) {
			body.setLinearVelocity(new GamaPoint());
			return;
		}
		GamaPoint p = new GamaPoint();
		body.getLinearVelocity(p);
		double currentSpeed = Math.atan2(p.x, p.y);
		if (currentSpeed <= 0) {
			body.setLinearVelocity(new GamaPoint(0.7 * speed, 0.7 * speed)); // ???
		} else {
			double ratio = speed / currentSpeed;
			p.setLocation(ratio * p.x, ratio * p.y, 0);
			body.setLinearVelocity(p);
		}
	}

	/**
	 * Gets the aabb.
	 *
	 * @param a
	 *            the a
	 * @return the aabb
	 */
	@getter (AABB)
	public IShape getAABB(final IAgent a) {
		IBody body = getBody(a);
		if (body == null) return null;
		return body.getAABB();
	}

	/**
	 * Gets the friction.
	 *
	 * @param a
	 *            the a
	 * @return the friction
	 */
	@getter (FRICTION)
	public Double getFriction(final IAgent a) {
		IBody body = getBody(a);
		if (body == null) return 00d;
		return (double) body.getFriction();
	}

	/**
	 * Sets the friction.
	 *
	 * @param a
	 *            the a
	 * @param friction
	 *            the friction
	 */
	@setter (FRICTION)
	public void setFriction(final IAgent a, final Double friction) {
		IBody body = getBody(a);
		if (body == null) return;
		body.setFriction(friction);
	}

	/**
	 * Gets the restitution.
	 *
	 * @param a
	 *            the a
	 * @return the restitution
	 */
	@getter (RESTITUTION)
	public Double getRestitution(final IAgent a) {
		IBody body = getBody(a);
		if (body == null) return 00d;
		return (double) body.getRestitution();
	}

	/**
	 * Sets the restitution.
	 *
	 * @param a
	 *            the a
	 * @param restitution
	 *            the restitution
	 */
	@setter (RESTITUTION)
	public void setRestitution(final IAgent a, final Double restitution) {
		IBody body = getBody(a);
		if (body == null) return;
		body.setRestitution(restitution);
	}

	/**
	 * Prim update geometry.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			doc = @doc ("This action must be called when the geometry of the agent changes in the simulation world and this change must be propagated to the physical world. "
					+ "The change of location (in either worlds) or the rotation due to physical forces do not count as changes, as they are already taken into account. "
					+ "However, a rotation in the simulation world need to be handled by calling this action. As it involves long operations (removing the agent from the physical world, "
					+ "then reinserting it with its new shape), this action should not be called too often."),
			name = UPDATE_BODY,
			args = {})
	public Object primUpdateGeometry(final IScope scope) {
		SimulationAgent sim = scope.getSimulation();
		if (sim instanceof PhysicalSimulationAgent) {
			((PhysicalSimulationAgent) sim).updateAgent(scope, scope.getAgent());
		}
		return null;
	}

	/**
	 * Prim contact added.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			doc = @doc ("This action can be redefined in order for the agent to implement a specific behavior when it comes into contact (collision) with another agent. "
					+ "It is automatically called by the physics simulation engine on both colliding agents. The default built-in behavior does nothing."),
			name = CONTACT_ADDED,
			args = { @arg (
					doc = @doc ("represents the other agent with which a collision has been detected"),
					name = OTHER,
					optional = false,
					type = IType.AGENT) })
	public Object primContactAdded(final IScope scope) {
		// Does nothing by default
		return null;
	}

	/**
	 * Prim contact destroyed.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			doc = @doc ("This action can be redefined in order for the agent to implement a specific behavior when a previous contact with another agent is removed. "
					+ "It is automatically called by the physics simulation engine on both colliding agents. The default built-in behavior does nothing."),
			name = CONTACT_REMOVED,
			args = { @arg (
					doc = @doc ("represents the other agent with which a collision has been detected"),
					name = OTHER,
					optional = false,
					type = IType.AGENT) })
	public Object primContactDestroyed(final IScope scope) {
		// Does nothing by default
		return null;
	}

	/***
	 * A class used to provide a temporary body to agents before their "bullet" one is built. It allows to store the
	 * information sent by the agent and to retrieve it once their actual body is being built
	 *
	 * @author drogoul
	 *
	 */
	public class FakeBody implements IBody<Object, Object, Object, GamaPoint> {

		/** The values. */
		public final Map<String, Object> values = new HashMap<>();

		@Override
		public float getFriction() {
			Double result = (Double) values.get(FRICTION);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public float getRestitution() {
			Double result = (Double) values.get(RESTITUTION);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public float getLinearDamping() {
			Double result = (Double) values.get(DAMPING);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public float getAngularDamping() {
			Double result = (Double) values.get(ANGULAR_DAMPING);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public GamaPoint getAngularVelocity(final GamaPoint v) {
			GamaPoint result = v == null ? new GamaPoint() : v;
			GamaPoint existing = (GamaPoint) values.get(ANGULAR_VELOCITY);
			if (existing == null) {
				result.setLocation(0, 0, 0);
			} else {
				result.setLocation(existing);
			}
			return result;
		}

		@Override
		public GamaPoint getLinearVelocity(final GamaPoint v) {
			GamaPoint result = v == null ? new GamaPoint() : v;
			GamaPoint existing = (GamaPoint) values.get(VELOCITY);
			if (existing == null) {
				result.setLocation(0, 0, 0);
			} else {
				result.setLocation(existing);
			}
			return result;
		}

		@Override
		public void setCCD(final boolean v) {
			values.put("CCD", v);
		}

		@Override
		public void setFriction(final Double friction) {
			values.put(FRICTION, friction);

		}

		@Override
		public void setRestitution(final Double restitution) {
			values.put(RESTITUTION, restitution);

		}

		@Override
		public void setDamping(final Double damping) {
			values.put(DAMPING, damping);
		}

		@Override
		public void setAngularDamping(final Double damping) {
			values.put(ANGULAR_DAMPING, damping);
		}

		@Override
		public void setAngularVelocity(final GamaPoint p) {
			values.put(ANGULAR_VELOCITY, p);
		}

		@Override
		public void setLinearVelocity(final GamaPoint p) {
			values.put(VELOCITY, p);
		}

		@Override
		public void setLocation(final GamaPoint loc) {

		}

		@Override
		public void clearForces() {}

		@Override
		public void applyImpulse(final GamaPoint impulse) {}

		@Override
		public void applyTorque(final GamaPoint torque) {}

		@Override
		public void applyForce(final GamaPoint force) {}

		@Override
		public void setMass(final Double mass) {
			values.put(MASS, mass);
		}

		@Override
		public float getMass() {
			Double result = (Double) values.get(MASS);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public IShape getAABB() { return null; }

		@Override
		public float getContactDamping() {
			Double result = (Double) values.get(CONTACT_DAMPING);
			return result != null ? result.floatValue() : 0f;
		}

		@Override
		public void setContactDamping(final Double damping) {
			values.put(CONTACT_DAMPING, damping);
		}

		@Override
		public Object getBody() { return this; }

		@Override
		public IAgent getAgent() { return null; }

		@Override
		public GamaPoint toVector(final GamaPoint v) {
			return v;
		}

		@Override
		public GamaPoint toGamaPoint(final GamaPoint v) {
			return v;
		}

		@Override
		public Object createAndInitializeBody(final Object shape, final Object world) {
			return this;
		}

	}
}
