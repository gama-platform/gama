## Support

#### **Installing optional diagramming or Copilot**

Use **Support > Install diagramming...** or **Support > Install Copilot...**
in a packaged GAMA installation. GAMA selects the feature and update sites,
resolves dependencies in the background, and opens an installation review without
requiring you to browse repositories or select individual plug-ins. Review the
changes, accept the applicable licenses, confirm trust when prompted, and restart
GAMA when Eclipse requests it. License and signature checks are not bypassed.

Diagramming installs the GAMA graphical modeling feature, including Graphiti,
GEF and its EMF dependencies, from the 2026-06 graphical modeling update site.
Copilot installs the optional `gama.feature.copilot` feature from its independent
update site, <https://gama-platform.org/gama.copilot/>, with the upstream GitHub
Copilot feature, its platform-specific agent and the GAMA integration plug-in. Both installers
use the Eclipse 2026-03 repository for required platform dependencies.
Already-installed features are reported rather than reinstalled; use
**Support > Check for Updates** to update them.
For Copilot, the installed check uses the GAMA wrapper feature: having only the
upstream extension installed does not prevent adding the GAMA integration.

These commands cover installation only. GitHub sign-in, Copilot entitlement,
and graphical/Copilot integration with GAML are separate from installation.
Before opening the workbench, the optional integration seeds Copilot's configuration preferences to
skip Quick Start and disable automatic What's New pages. Existing settings are
preserved; users can re-enable What's New in Copilot's General preferences.
The Quick Start marker is based on Copilot 0.21 and should be reviewed on upgrades.
Network, unavailable-feature and dependency-resolution failures are reported;
they do not trigger installation from another update channel.

Copilot packaging, dependency baselines, publishing and integration-specific issues
are maintained in [gama.copilot](https://github.com/gama-platform/gama.copilot).
The feature is no longer built or published by GAMA's Maven reactor.
Use that repository's documented build to publish Copilot updates independently.
The wrapper includes the upstream feature rather than duplicating its
platform-specific plug-in list.

For distribution maintainers: the Copilot update site must be published before
distributing the installer command. Its integration uses the
`gama.ui.application.startup` extension point for pre-workbench preference setup;
generic GAML editor extension support remains in GAMA.
Rebuild and publish the graphical modeling repository as well to apply its
transitive-dependency packaging setting. These baselines must be reviewed when
GAMA moves to a different Eclipse release or graphical modeling update channel.

#### **Do you think you found a bug?**

* **Ensure the bug was not already reported** by searching on GitHub under [Issues](https://github.com/gama-platform/gama/issues).

* If you're unable to find an open issue addressing the problem, [open a new one](https://github.com/gama-platform/gama/issues/new). Be sure to include a **title and clear description**, as much relevant information as possible, and a **code sample** or a **test case** demonstrating the expected behavior that is not occurring.

#### **Do you have questions about your models?**

* Ask any question about how to use GAMA in the [gama-platform mailing list](https://groups.google.com/forum/#!forum/gama-platform).

* Consult the documentation on the [website](http://gama-platform.org) or, alternatively, on the [wiki](https://github.com/gama-platform/gama/wiki).
</br>


Thanks!

GAMA Team
