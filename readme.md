This repo contains several continuations of the original SGCraft mod developed by gcewing.

Downloads are available here:  https://ore.spongepowered.org/Dockter/SGCraft

The following branches are active:
- 1.12.2 -> Active fixes for 1.12.2
- api-7 -> All fixes from 1.12.2 branch plus Sponge permissions checks and event firings.
- feature/zpm -> Everything from api-7 + a custom zpm module implementation specifically designed for usage with IC2 and the Almura mod.

feature/zpm branch, use this:
- vm options: -Xincgc -Xms1024M -Xmx2048M -Dfml.coreMods.load=org.spongepowered.mod.SpongeCoremod
- program arguments: --noCoreSearch --mixin mixins.sgcraft.json

Any questions please create an issue.

Thanks.
