package dev.blinkwhite.remoteinventory.network.payload;

//#if MC >= 12005
import dev.blinkwhite.remoteinventory.Reference;
import dev.blinkwhite.remoteinventory.enums.ResultType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class GetItemResultPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GetItemResultPayload> TYPE = new CustomPacketPayload.Type<>(
        //#if MC >= 12105
        net.minecraft.resources.Identifier.fromNamespaceAndPath(Reference.MOD_ID, "get_item_result")
        //#elseif MC >= 12101
        //$$ net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "get_item_result")
        //#else
        //$$ new net.minecraft.resources.ResourceLocation(Reference.MOD_ID, "get_item_result")
        //#endif
    );

    private final BlockPos pos;
    private final ResultType resultType;

    public GetItemResultPayload(BlockPos pos, ResultType resultType) {
        this.pos = pos;
        this.resultType = resultType;
    }

    public BlockPos getPos() { return pos; }
    public ResultType getResultType() { return resultType; }

    public static GetItemResultPayload decode(ByteBuf buf) {
        FriendlyByteBuf wrapped = (FriendlyByteBuf) buf;
        return new GetItemResultPayload(wrapped.readBlockPos(), wrapped.readEnum(ResultType.class));
    }

    public void write(ByteBuf buf) {
        FriendlyByteBuf wrapped = (FriendlyByteBuf) buf;
        wrapped.writeBlockPos(pos);
        wrapped.writeEnum(resultType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
//#else
//$$ // Pre-1.20.5: payload reading/writing handled directly in ChannelHandler
//$$ class GetItemResultPayload {}
//#endif
