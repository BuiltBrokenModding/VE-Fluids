package com.builtbroken.mc.fluids.client;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//From gigaherz
//https://gist.github.com/gigaherz/2a79bbd8e99286e54db5f3f267f98829
public class QuadTransformer
{

    public static BakedQuad processOne(BakedQuad input, Matrix4f transform)
    {
        VertexFormat fmt = input.getFormat();
        int positionIndex;
        VertexFormatElement positionElement = null;
        for (positionIndex = 0; positionIndex < fmt.getElementCount(); positionIndex++)
        {
            VertexFormatElement el = fmt.getElement(positionIndex);
            if (el.getUsage() == VertexFormatElement.EnumUsage.POSITION)
            {
                positionElement = el;
                break;
            }
        }
        if (positionIndex == fmt.getElementCount() || positionElement == null)
        {
            throw new RuntimeException("WAT? Position not found");
        }
        if (positionElement.getType() != VertexFormatElement.EnumType.FLOAT)
        {
            throw new RuntimeException("WAT? Position not FLOAT");
        }
        if (positionElement.getSize() < 3)
        {
            throw new RuntimeException("WAT? Position not 3D");
        }
        int positionOffset = fmt.getOffset(positionIndex);

        int[] data = input.getVertexData();
        float x = Float.intBitsToFloat(data[positionOffset]);
        float y = Float.intBitsToFloat(data[positionOffset + 1]);
        float z = Float.intBitsToFloat(data[positionOffset + 2]);

        Vector4f pos = new Vector4f(x, y, z, 1);
        transform.transform(pos);
        pos.scale(1 / pos.w);

        data[positionOffset] = Float.floatToRawIntBits(pos.x);
        data[positionOffset + 1] = Float.floatToRawIntBits(pos.y);
        data[positionOffset + 2] = Float.floatToRawIntBits(pos.z);

        return new BakedQuad(data, input.getTintIndex(), input.getFace(), input.getSprite(), input.shouldApplyDiffuseLighting(), fmt);
    }

    public static List<BakedQuad> processMany(List<BakedQuad> inputs, Matrix4f transform)
    {
        if (inputs.size() == 0)
        {
            return Collections.emptyList();
        }

        VertexFormat fmt = inputs.get(0).getFormat();
        int positionIndex;
        VertexFormatElement positionElement = null;
        for (positionIndex = 0; positionIndex < fmt.getElementCount(); positionIndex++)
        {
            VertexFormatElement el = fmt.getElement(positionIndex);
            if (el.getUsage() == VertexFormatElement.EnumUsage.POSITION)
            {
                positionElement = el;
                break;
            }
        }
        if (positionIndex == fmt.getElementCount() || positionElement == null)
        {
            throw new RuntimeException("WAT? Position not found");
        }
        if (positionElement.getType() != VertexFormatElement.EnumType.FLOAT)
        {
            throw new RuntimeException("WAT? Position not FLOAT");
        }
        if (positionElement.getSize() < 3)
        {
            throw new RuntimeException("WAT? Position not 3D");
        }
        int positionOffset = fmt.getOffset(positionIndex);

        List<BakedQuad> outputs = Lists.newArrayList();
        for (BakedQuad input : inputs)
        {
            int[] data = input.getVertexData();
            float x = Float.intBitsToFloat(data[positionOffset]);
            float y = Float.intBitsToFloat(data[positionOffset + 1]);
            float z = Float.intBitsToFloat(data[positionOffset + 2]);

            Vector4f pos = new Vector4f(x, y, z, 1);
            transform.transform(pos);
            pos.scale(1 / pos.w);

            int[] outData = Arrays.copyOf(data, data.length);
            outData[positionOffset] = Float.floatToRawIntBits(pos.x);
            outData[positionOffset + 1] = Float.floatToRawIntBits(pos.y);
            outData[positionOffset + 2] = Float.floatToRawIntBits(pos.z);

            outputs.add(new BakedQuad(data, input.getTintIndex(), input.getFace(), input.getSprite(), input.shouldApplyDiffuseLighting(), fmt));
        }
        return outputs;
    }

    public static void processManyInPlace(List<BakedQuad> inputs, Matrix4f transform)
    {
        if (inputs.size() == 0)
        {
            return;
        }

        VertexFormat fmt = inputs.get(0).getFormat();
        int positionIndex;
        VertexFormatElement positionElement = null;
        for (positionIndex = 0; positionIndex < fmt.getElementCount(); positionIndex++)
        {
            VertexFormatElement el = fmt.getElement(positionIndex);
            if (el.getUsage() == VertexFormatElement.EnumUsage.POSITION)
            {
                positionElement = el;
                break;
            }
        }
        if (positionIndex == fmt.getElementCount() || positionElement == null)
        {
            throw new RuntimeException("WAT? Position not found");
        }
        if (positionElement.getType() != VertexFormatElement.EnumType.FLOAT)
        {
            throw new RuntimeException("WAT? Position not FLOAT");
        }
        if (positionElement.getSize() < 3)
        {
            throw new RuntimeException("WAT? Position not 3D");
        }
        int positionOffset = fmt.getOffset(positionIndex);

        for (BakedQuad input : inputs)
        {
            int[] data = input.getVertexData();
            float x = Float.intBitsToFloat(data[positionOffset]);
            float y = Float.intBitsToFloat(data[positionOffset + 1]);
            float z = Float.intBitsToFloat(data[positionOffset + 2]);

            Vector4f pos = new Vector4f(x, y, z, 1);
            transform.transform(pos);
            pos.scale(1 / pos.w);

            data[positionOffset] = Float.floatToRawIntBits(pos.x);
            data[positionOffset + 1] = Float.floatToRawIntBits(pos.y);
            data[positionOffset + 2] = Float.floatToRawIntBits(pos.z);
        }
    }
}