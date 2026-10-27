package com.eisiadev.avatarofemptiness.client.model;

import com.eisiadev.avatarofemptiness.AvatarOfEmptinessMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BbModelLoader {
    private static final BufferBuilder BUILDER = new BufferBuilder(2048);

    private record Tex(ResourceLocation id, float uvW, float uvH) {}

    private record Anim(Vector3f rot, Vector3f pos, Vector3f scale) {}

    private record Quad(int texture, boolean head, float[] xyz, float[] uv) {}

    public static Optional<BbModel> load(ResourceManager resources, ResourceLocation file, String textureKey) {
        Optional<Resource> res = resources.getResource(file);
        if (res.isEmpty()) {
            AvatarOfEmptinessMod.LOGGER.error("bbmodel not found: {}", file);
            return Optional.empty();
        }
        try (Reader reader = res.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return Optional.of(bake(root, textureKey));
        } catch (Exception e) {
            AvatarOfEmptinessMod.LOGGER.error("Failed to load bbmodel {}", file, e);
            return Optional.empty();
        }
    }

    private static BbModel bake(JsonObject root, String key) throws IOException {
        float resW = 16, resH = 16;
        if (root.has("resolution")) {
            JsonObject r = root.getAsJsonObject("resolution");
            resW = r.get("width").getAsFloat();
            resH = r.get("height").getAsFloat();
        }

        List<Tex> textures = new ArrayList<>();
        if (root.has("textures")) {
            JsonArray arr = root.getAsJsonArray("textures");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject t = arr.get(i).getAsJsonObject();
                float uvW = t.has("uv_width") ? t.get("uv_width").getAsFloat() : resW;
                float uvH = t.has("uv_height") ? t.get("uv_height").getAsFloat() : resH;
                ResourceLocation id = AvatarOfEmptinessMod.id("bbmodel/" + key + "/" + i);
                String src = t.has("source") ? t.get("source").getAsString() : "";
                int comma = src.indexOf(',');
                if (src.startsWith("data:") && comma > 0) {
                    byte[] png = Base64.getDecoder().decode(src.substring(comma + 1));
                    NativeImage image = NativeImage.read(new ByteArrayInputStream(png));
                    Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
                } else {
                    AvatarOfEmptinessMod.LOGGER.warn("bbmodel {} texture {} has no embedded image", key, i);
                    id = null;
                }
                textures.add(new Tex(id, uvW, uvH));
            }
        }

        Map<String, JsonObject> elements = new HashMap<>();
        if (root.has("elements")) {
            for (JsonElement e : root.getAsJsonArray("elements")) {
                JsonObject o = e.getAsJsonObject();
                elements.put(o.get("uuid").getAsString(), o);
            }
        }

        Map<String, Anim> pose = readPose(root);

        List<Quad> quads = new ArrayList<>();
        Vector3f headPivot = new Vector3f();
        boolean[] foundHead = {false};
        if (root.has("outliner")) {
            for (JsonElement node : root.getAsJsonArray("outliner")) {
                walk(node, new Matrix4f(), false, elements, pose, textures, quads, headPivot, foundHead);
            }
        }

        Vector3f min = new Vector3f(Float.MAX_VALUE), max = new Vector3f(-Float.MAX_VALUE);
        for (Quad q : quads) {
            if (q.head()) continue;
            for (int i = 0; i < 4; i++) {
                min.min(new Vector3f(q.xyz()[i * 3], q.xyz()[i * 3 + 1], q.xyz()[i * 3 + 2]));
                max.max(new Vector3f(q.xyz()[i * 3], q.xyz()[i * 3 + 1], q.xyz()[i * 3 + 2]));
            }
        }
        if (min.x > max.x) {
            min.set(-0.5f);
            max.set(0.5f);
        }
        Vector3f center = new Vector3f(min).add(max).mul(0.5f);
        Vector3f size = new Vector3f(max).sub(min);
        float radius = 0f;
        for (Quad q : quads) {
            if (q.head()) continue;
            for (int i = 0; i < 4; i++) {
                float dx = q.xyz()[i * 3] - center.x, dy = q.xyz()[i * 3 + 1] - center.y, dz = q.xyz()[i * 3 + 2] - center.z;
                radius = Math.max(radius, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
            }
        }
        Vector3f pivotRel = new Vector3f(headPivot).sub(center);
        float hx = 0f, hz = 0f;
        int hn = 0;
        for (Quad q : quads) {
            if (!q.head()) continue;
            for (int i = 0; i < 4; i++) {
                hx += q.xyz()[i * 3] - headPivot.x;
                hz += q.xyz()[i * 3 + 2] - headPivot.z;
                hn++;
            }
        }
        float headForward = hn > 0 ? (float) Math.atan2(hz / hn, hx / hn) : 0f;

        List<BbModel.Part> body = new ArrayList<>();
        List<BbModel.Part> head = new ArrayList<>();
        for (int part = 0; part < 2; part++) {
            boolean isHead = part == 1;
            for (int tex = 0; tex < textures.size(); tex++) {
                int count = 0;
                for (Quad q : quads) {
                    if (q.head() == isHead && q.texture() == tex) count++;
                }
                if (count == 0) continue;
                Vector3f off = isHead ? headPivot : center;
                BUILDER.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                for (Quad q : quads) {
                    if (q.head() != isHead || q.texture() != tex) continue;
                    for (int i = 0; i < 4; i++) {
                        BUILDER.vertex(q.xyz()[i * 3] - off.x, q.xyz()[i * 3 + 1] - off.y, q.xyz()[i * 3 + 2] - off.z)
                                .uv(q.uv()[i * 2], q.uv()[i * 2 + 1])
                                .color(1f, 1f, 1f, 1f)
                                .endVertex();
                    }
                }
                BufferBuilder.RenderedBuffer rendered = BUILDER.end();
                VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
                vb.bind();
                vb.upload(rendered);
                VertexBuffer.unbind();
                (isHead ? head : body).add(new BbModel.Part(tex, vb));
            }
        }

        ResourceLocation[] texIds = new ResourceLocation[textures.size()];
        for (int i = 0; i < texIds.length; i++) texIds[i] = textures.get(i).id();
        AvatarOfEmptinessMod.LOGGER.info("Baked bbmodel {}: {} quads, size {}, radius {}", key, quads.size(), size, radius);
        return new BbModel(body, head, texIds, foundHead[0] ? pivotRel : new Vector3f(), headForward, center, size, radius);
    }

    private static void walk(JsonElement node, Matrix4f parent, boolean inHead, Map<String, JsonObject> elements,
                             Map<String, Anim> pose, List<Tex> textures, List<Quad> out,
                             Vector3f headPivot, boolean[] foundHead) {
        if (node.isJsonPrimitive()) {
            JsonObject el = elements.get(node.getAsString());
            if (el != null && (!el.has("visibility") || el.get("visibility").getAsBoolean())) {
                cube(el, parent, inHead, textures, out);
            }
            return;
        }
        if (!node.isJsonObject()) return;
        JsonObject g = node.getAsJsonObject();
        String name = g.has("name") ? g.get("name").getAsString() : "";
        String lname = name.toLowerCase(Locale.ROOT);
        if (lname.equals("hitbox") || lname.startsWith("b_") || lname.startsWith("p_")) return;
        if (g.has("visibility") && !g.get("visibility").getAsBoolean()) return;

        Vector3f origin = vec(g, "origin", 0f);
        Vector3f baseRot = vec(g, "rotation", 0f);
        Anim a = pose.get(g.has("uuid") ? g.get("uuid").getAsString() : "");

        float rx = baseRot.x, ry = baseRot.y, rz = baseRot.z;
        float px = 0, py = 0, pz = 0;
        float sx = 1, sy = 1, sz = 1;
        if (a != null) {
            rx -= a.rot().x;
            ry -= a.rot().y;
            rz += a.rot().z;
            px = -a.pos().x;
            py = a.pos().y;
            pz = a.pos().z;
            sx = a.scale().x;
            sy = a.scale().y;
            sz = a.scale().z;
        }

        Matrix4f m = new Matrix4f(parent)
                .translate(origin.x + px, origin.y + py, origin.z + pz)
                .rotateZ((float) Math.toRadians(rz))
                .rotateY((float) Math.toRadians(ry))
                .rotateX((float) Math.toRadians(rx))
                .scale(sx, sy, sz)
                .translate(-origin.x, -origin.y, -origin.z);

        boolean head = inHead || lname.startsWith("h_");
        if (head && !inHead && !foundHead[0]) {
            Vector4f p = m.transform(new Vector4f(origin.x, origin.y, origin.z, 1f));
            headPivot.set(p.x / 16f, p.y / 16f, p.z / 16f);
            foundHead[0] = true;
        }

        if (g.has("children")) {
            for (JsonElement c : g.getAsJsonArray("children")) {
                walk(c, m, head, elements, pose, textures, out, headPivot, foundHead);
            }
        }
    }

    private static final String[] FACES = {"north", "south", "west", "east", "up", "down"};

    private static void cube(JsonObject el, Matrix4f bone, boolean head, List<Tex> textures, List<Quad> out) {
        String type = el.has("type") ? el.get("type").getAsString() : "cube";
        if (!type.equals("cube")) return;
        float inflate = el.has("inflate") ? el.get("inflate").getAsFloat() : 0f;
        Vector3f from = vec(el, "from", 0f).sub(inflate, inflate, inflate);
        Vector3f to = vec(el, "to", 0f).add(inflate, inflate, inflate);
        float x1 = from.x, x2 = to.x;
        float y1 = from.y, y2 = to.y;
        float z1 = from.z, z2 = to.z;

        Vector3f origin = vec(el, "origin", 0f);
        Vector3f rot = vec(el, "rotation", 0f);
        Matrix4f m = new Matrix4f(bone)
                .translate(origin.x, origin.y, origin.z)
                .rotateZ((float) Math.toRadians(rot.z))
                .rotateY((float) Math.toRadians(rot.y))
                .rotateX((float) Math.toRadians(rot.x))
                .translate(-origin.x, -origin.y, -origin.z);

        if (!el.has("faces")) return;
        JsonObject faces = el.getAsJsonObject("faces");
        for (String face : FACES) {
            if (!faces.has(face)) continue;
            JsonObject f = faces.getAsJsonObject(face);
            if (!f.has("texture") || f.get("texture").isJsonNull()) continue;
            JsonElement texEl = f.get("texture");
            if (!texEl.isJsonPrimitive() || !texEl.getAsJsonPrimitive().isNumber()) continue;
            int tex = texEl.getAsInt();
            if (tex < 0 || tex >= textures.size()) continue;
            JsonArray uvArr = f.getAsJsonArray("uv");
            if (uvArr == null || uvArr.size() < 4) continue;

            float[][] c = corners(face, x1, x2, y1, y2, z1, z2);
            Tex t = textures.get(tex);
            float u1 = uvArr.get(0).getAsFloat() / t.uvW(), v1 = uvArr.get(1).getAsFloat() / t.uvH();
            float u2 = uvArr.get(2).getAsFloat() / t.uvW(), v2 = uvArr.get(3).getAsFloat() / t.uvH();
            float[][] uvs = {{u1, v1}, {u2, v1}, {u2, v2}, {u1, v2}};
            int rotSteps = f.has("rotation") ? (f.get("rotation").getAsInt() / 90) & 3 : 0;

            float[] xyz = new float[12];
            float[] uv = new float[8];
            for (int i = 0; i < 4; i++) {
                Vector4f p = m.transform(new Vector4f(c[i][0], c[i][1], c[i][2], 1f));
                xyz[i * 3] = p.x / 16f;
                xyz[i * 3 + 1] = p.y / 16f;
                xyz[i * 3 + 2] = p.z / 16f;
                float[] tuv = uvs[(i + 4 - rotSteps) & 3];
                uv[i * 2] = tuv[0];
                uv[i * 2 + 1] = tuv[1];
            }
            swap(xyz, 3, 9, 3);
            swap(uv, 2, 6, 2);
            out.add(new Quad(tex, head, xyz, uv));
        }
    }

    private static void swap(float[] a, int i, int j, int n) {
        for (int k = 0; k < n; k++) {
            float t = a[i + k];
            a[i + k] = a[j + k];
            a[j + k] = t;
        }
    }

    private static float[][] corners(String face, float x1, float x2, float y1, float y2, float z1, float z2) {
        return switch (face) {
            case "north" -> new float[][]{{x2, y2, z1}, {x1, y2, z1}, {x1, y1, z1}, {x2, y1, z1}};
            case "south" -> new float[][]{{x1, y2, z2}, {x2, y2, z2}, {x2, y1, z2}, {x1, y1, z2}};
            case "west" -> new float[][]{{x1, y2, z1}, {x1, y2, z2}, {x1, y1, z2}, {x1, y1, z1}};
            case "east" -> new float[][]{{x2, y2, z2}, {x2, y2, z1}, {x2, y1, z1}, {x2, y1, z2}};
            case "up" -> new float[][]{{x1, y2, z1}, {x2, y2, z1}, {x2, y2, z2}, {x1, y2, z2}};
            default -> new float[][]{{x1, y1, z2}, {x2, y1, z2}, {x2, y1, z1}, {x1, y1, z1}};
        };
    }

    private static Map<String, Anim> readPose(JsonObject root) {
        Map<String, Anim> pose = new HashMap<>();
        if (!root.has("animations")) return pose;
        JsonArray anims = root.getAsJsonArray("animations");
        JsonObject chosen = null;
        for (JsonElement e : anims) {
            JsonObject a = e.getAsJsonObject();
            if (chosen == null) chosen = a;
            if (a.has("name") && a.get("name").getAsString().equalsIgnoreCase("idle")) {
                chosen = a;
                break;
            }
        }
        if (chosen == null || !chosen.has("animators")) return pose;
        JsonObject animators = chosen.getAsJsonObject("animators");
        for (Map.Entry<String, JsonElement> entry : animators.entrySet()) {
            JsonObject an = entry.getValue().getAsJsonObject();
            if (!an.has("keyframes")) continue;
            Vector3f rot = new Vector3f(), pos = new Vector3f(), scale = new Vector3f(1f);
            float tRot = Float.MAX_VALUE, tPos = Float.MAX_VALUE, tScale = Float.MAX_VALUE;
            for (JsonElement kfE : an.getAsJsonArray("keyframes")) {
                JsonObject kf = kfE.getAsJsonObject();
                String channel = kf.get("channel").getAsString();
                float time = kf.has("time") ? kf.get("time").getAsFloat() : 0f;
                JsonArray dps = kf.getAsJsonArray("data_points");
                if (dps == null || dps.isEmpty()) continue;
                JsonObject dp = dps.get(0).getAsJsonObject();
                Vector3f v = new Vector3f(num(dp, "x"), num(dp, "y"), num(dp, "z"));
                switch (channel) {
                    case "rotation" -> { if (time < tRot) { tRot = time; rot.set(v); } }
                    case "position" -> { if (time < tPos) { tPos = time; pos.set(v); } }
                    case "scale" -> { if (time < tScale) { tScale = time; scale.set(v); } }
                    default -> { }
                }
            }
            pose.put(entry.getKey(), new Anim(rot, pos, scale));
        }
        return pose;
    }

    private static float num(JsonObject o, String key) {
        if (!o.has(key)) return 0f;
        JsonElement e = o.get(key);
        try {
            if (e.getAsJsonPrimitive().isNumber()) return e.getAsFloat();
            String s = e.getAsString().trim();
            return s.isEmpty() ? 0f : Float.parseFloat(s);
        } catch (Exception ex) {
            return 0f;
        }
    }

    private static Vector3f vec(JsonObject o, String key, float def) {
        if (!o.has(key) || !o.get(key).isJsonArray()) return new Vector3f(def);
        JsonArray a = o.getAsJsonArray(key);
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private BbModelLoader() {}
}
