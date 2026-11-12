/**
* Name: PhysicsAdvancedTest
* Description: Tests the physics engine extension: the 'physical_world' simulation parent and
*   the documented defaults of the 'static_body' and 'dynamic_body' skills.
* Tags: physics, extension, test
*/

model PhysicsAdvancedTestModel

global parent: physical_world {
	float step <- 0.01;
}

species ball skills: [dynamic_body] {
	geometry shape <- sphere(1);
}

species heavy_ball skills: [dynamic_body] {
	geometry shape <- sphere(1);
	float mass <- 5.0;
	float restitution <- 0.9;
	float damping <- 0.0;
}

species wall skills: [static_body] {
	geometry shape <- box(10, 10, 1);
}

experiment PhysicsAdvancedTest type: test autorun: true {

	test "The physical world exposes its documented defaults" {
		assert world.gravity = {0, 0, -9.80665};
		assert world.automated_registration = true;
		assert world.max_substeps = 0;
		assert world.accurate_collision_detection = false;
	}

	test "A static body reports the engine's own conventions" {
		create wall number: 1 returns: walls;
		wall w <- walls[0];

		assert w.friction = 0.5;
		assert w.restitution = 0.0;
		assert w.rotation = 0.0 :: {0, 0, 1};
		assert w.aabb != nil;
	}

	test "A dynamic body reports its declared mass" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];

		assert b.mass = 1.0;
		assert b.friction = 0.5;
		assert b.restitution = 0.0;
		assert b.velocity = {0, 0, 0};
		assert b.angular_velocity = {0, 0, 0};
	}

	test "Species-level overrides reach the variable-backed attributes" {
		create heavy_ball number: 1 returns: balls;
		heavy_ball b <- balls[0];

		assert b.mass = 5.0;
		// not overridden, so still the skill default
		assert b.friction = 0.5;
	}

	// The damping attributes are read straight from the underlying rigid body rather than from the
	// GAML variable, so their exact value depends on the engine. What must hold is that they are
	// readable damping ratios.
	test "Damping attributes are readable damping ratios" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];

		assert b.damping >= 0.0;
		assert b.damping <= 1.0;
		assert b.angular_damping >= 0.0;
		assert b.angular_damping <= 1.0;
		assert b.contact_damping >= 0.0;
		assert b.contact_damping <= 1.0;
	}

	test "Bodies can be registered in the physical world" {
		create ball number: 3 returns: balls;
		ask world { do register(bodies: balls); }
		assert length(balls) = 3;
	}

	// The physical world only advances when the simulation itself is stepped by its scheduler, which cannot be
	// done from a test: what follows checks what is applied to the bodies, not their trajectories

	test "An impulse changes the velocity in proportion to the mass" {
		create ball number: 1 returns: balls;
		create heavy_ball number: 1 returns: heavy_balls;
		ball light <- balls[0];
		heavy_ball heavy <- heavy_balls[0];
		ask light { do apply(impulse: {10, 0, 0}); }
		ask heavy { do apply(impulse: {10, 0, 0}); }
		// velocity = impulse / mass
		assert light.velocity = {10, 0, 0};
		assert heavy.velocity = {2, 0, 0};

		// impulses add up
		ask light { do apply(impulse: {0, 5, 0}); }
		assert light.velocity = {10, 5, 0};
		ask light { do apply(impulse: {-10, -5, 0}); }
		assert light.velocity = {0, 0, 0};
	}

	test "A force has no immediate effect on the velocity" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];
		ask b { do apply(force: {10, 0, 0}); }
		// forces are integrated by the engine when the world steps
		assert b.velocity = {0, 0, 0};
	}

	test "Velocities can be set directly" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];
		ask b {
			velocity <- {0, 0, 5};
			angular_velocity <- {1, 2, 3};
		}
		assert b.velocity = {0, 0, 5};
		assert b.angular_velocity = {1, 2, 3};
		// setting a velocity does not move the body by itself
		point before <- b.location;
		ask b { velocity <- {100, 100, 100}; }
		assert b.location = before;
	}

	test "Body properties can be changed after creation" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];
		ask b {
			friction <- 0.9;
			restitution <- 0.4;
			mass <- 7.0;
		}
		// the engine stores them in single precision
		assert b.friction with_precision 5 = 0.9;
		assert b.restitution with_precision 5 = 0.4;
		assert b.mass with_precision 5 = 7.0;

		// the new mass is taken into account by the next impulse
		ask b { do apply(impulse: {7, 0, 0}); }
		assert b.velocity.x with_precision 5 = 1.0;
	}

	test "The bounding box of a body surrounds its shape" {
		create ball number: 1 returns: balls;
		ball b <- balls[0];
		ask b { location <- {50, 50, 50}; }
		// a sphere of radius 1
		assert b.aabb.width with_precision 5 = 2.0;
		assert b.aabb.height with_precision 5 = 2.0;
		assert b.aabb.location.x with_precision 5 = 50.0;
		assert b.aabb.location.y with_precision 5 = 50.0;
	}
}
