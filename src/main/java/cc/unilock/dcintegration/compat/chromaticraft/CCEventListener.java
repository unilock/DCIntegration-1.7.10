package cc.unilock.dcintegration.compat.chromaticraft;

import Reika.ChromatiCraft.API.CrystalElementAccessor;
import Reika.ChromatiCraft.API.Event.ProgressionEvent;
import Reika.ChromatiCraft.Magic.Progression.ProgressStage;
import Reika.ChromatiCraft.Registry.ChromaResearch;
import cc.unilock.dcintegration.util.ForgeMessageUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import net.minecraft.entity.player.EntityPlayerMP;

import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.INSTANCE;
import static de.erdbeerbaerlp.dcintegration.common.DiscordIntegration.LOGGER;

public class CCEventListener {
    // TODO: configurable
    private static final String CC_COLOR_MSG = "> **%player%** has learned something new: \"%progressName%\"";
    private static final String CC_DIMSTRUCT_MSG = "> **%player%** has learned something new: \"%progressName%\"";
    private static final String CC_FRAGMENT_MSG = "> **%player%** has learned something new: *%progressName%*";
    private static final String CC_PROGRESS_MSG = "> **%player%** has learned something new: __%progressName%__";

    @SubscribeEvent
    public void onProgressionEvent(ProgressionEvent ev) {
        EntityPlayerMP player = (EntityPlayerMP) ev.entityPlayer;

        if (INSTANCE != null) {
            if (LinkManager.isPlayerLinked(player.getUniqueID()) && LinkManager.getLink(null, player.getUniqueID()).settings.hideFromDiscord)
                return;
            LinkManager.checkGlobalAPI(player.getUniqueID());

            String title = null;
            String desc = null;
            String template = null;

            if (ev.type == ProgressionEvent.ResearchType.COLOR && !CC_COLOR_MSG.trim().isEmpty()) {
                CrystalElementAccessor.CrystalElementProxy color = CrystalElementAccessor.getByEnum(ev.researchKey);

                title = color.displayName();
                desc = "A new form of crystal energy";
                template = CC_COLOR_MSG;
            } else if (ev.type == ProgressionEvent.ResearchType.DIMSTRUCT && !CC_DIMSTRUCT_MSG.trim().isEmpty()) {
                CrystalElementAccessor.CrystalElementProxy color = CrystalElementAccessor.getByEnum(ev.researchKey);

                title = color.displayName() + " Core";
                desc = "Another piece of the puzzle";
                template = CC_DIMSTRUCT_MSG;
            } else if (ev.type == ProgressionEvent.ResearchType.FRAGMENT && !CC_FRAGMENT_MSG.trim().isEmpty()) {
                ChromaResearch research = ChromaResearch.getByName(ev.researchKey);

                title = research.getTitle();
                desc = "Something new to investigate";
                template = CC_FRAGMENT_MSG;
            } else if (ev.type == ProgressionEvent.ResearchType.PROGRESS && !CC_PROGRESS_MSG.trim().isEmpty()) {
                ProgressStage stage;

                try {
                    stage = ProgressStage.valueOf(ev.researchKey);

                    if (stage.getShareability() == ProgressStage.Shareability.ALWAYS || stage.getShareability() == ProgressStage.Shareability.PROXIMITY) {
                        title = stage.getTitle();
                        desc = stage.getShortDesc();
                        template = CC_PROGRESS_MSG;
                    } else {
                        LOGGER.trace("CC: Ignoring non-shareable ProgressStage");
                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error("CC: Not a ProgressStage? : " + ev.researchKey);
                }
            }

            if (title != null && desc != null && template != null) {
                INSTANCE.sendMessage(template
                    .replace("%player%", ForgeMessageUtils.formatPlayerName(player))
                    .replace("%progressName%", title)
                    .replace("%progressDesc%", desc) // NOTE: often somewhat spoilery, and isn't normally displayed in chat
                    .replace("\\n", "\n")
                );
            }
        }
    }
}
