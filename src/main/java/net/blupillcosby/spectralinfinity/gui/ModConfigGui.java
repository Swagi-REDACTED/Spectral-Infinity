package net.blupillcosby.spectralinfinity.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.blupillcosby.spectralinfinity.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModConfigGui extends LightweightGuiDescription {
    public static final int COLOR_BACKGROUND = 0xFF0A0E1A;
    public static final int COLOR_CARD = 0xFF151B2B;
    public static final int COLOR_ACCENT = 0xFF3D92FF;
    public static final int COLOR_DANGER = 0xFFFF4444;
    public static final int COLOR_TEXT = 0xFFF5F5F5;
    public static final int COLOR_TEXT_DIM = 0xFFB0B0B0;

    private final WPlainPanel root;
    private WPanel currentDialog;

    @SuppressWarnings("this-escape")
    public ModConfigGui() {
        root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(320, 260);
        root.setInsets(Insets.ROOT_PANEL);

        WLabel title = new WLabel(Component.literal("SPECTRAL INFINITY"), COLOR_ACCENT);
        title.setHorizontalAlignment(HorizontalAlignment.CENTER);
        root.add(title, 0, 10, 320, 20);

        ModConfig config = ModConfig.get();

        // General Settings Card
        WPlainPanel generalCard = createCard(300, 45);
        root.add(generalCard, 10, 35);

        WToggleButton allArrowsButton = new BlueToggleButton(Component.literal("Enable for All Arrow Types"));
        allArrowsButton.setToggle(config.allArrows.booleanValue());
        allArrowsButton.setOnToggle(config.allArrows::setValue);
        generalCard.add(allArrowsButton, 10, 10, 280, 30);

        // Whitelist Section
        WLabel whitelistTitle = new WLabel(Component.literal("ARROW WHITELIST"), COLOR_TEXT_DIM);
        root.add(whitelistTitle, 15, 85, 300, 15);

        WPlainPanel whitelistCard = createCard(300, 120);
        root.add(whitelistCard, 10, 100);

        // List entries
        WBox listContainer = new WBox(Axis.VERTICAL);
        listContainer.setSpacing(2);

        // Add Plus Button at top of list
        FlatButton openPickerButton = new FlatButton(Component.literal("+ ADD ARROW"), COLOR_ACCENT);
        openPickerButton.setOnClick(() -> openDialog(new ArrowPicker(keys -> {
            for (String key : keys) {
                if (!config.arrowWhitelist.contains(key)) {
                    config.arrowWhitelist.add(key);
                }
            }
            Minecraft.getInstance().setScreen(new ModConfigScreen(null));
        })));
        listContainer.add(openPickerButton, 280, 22);

        // Current entries - Group by Label to deduplicate visually
        Map<String, List<String>> labelToIds = new LinkedHashMap<>();
        for (String idStr : config.arrowWhitelist) {
            ItemStack stack = getItemStackFromId(idStr);
            if (stack != null) {
                String label = stack.getHoverName().getString();
                labelToIds.computeIfAbsent(label, k -> new ArrayList<>()).add(idStr);
            }
        }

        for (Map.Entry<String, List<String>> entryGroup : labelToIds.entrySet()) {
            String label = entryGroup.getKey();
            List<String> idsForLabel = entryGroup.getValue();
            
            WPlainPanel entry = new WPlainPanel();
            entry.setSize(280, 24);

            ItemStack displayStack = getItemStackFromId(idsForLabel.get(0));
            if (displayStack != null) {
                WLabel idLabel = new WLabel(displayStack.getHoverName(), COLOR_TEXT);
                entry.add(idLabel, 30, 4, 200, 15);

                WItem itemIcon = new WItem(displayStack);
                entry.add(itemIcon, 5, 4, 16, 16);

                FlatButton removeButton = new FlatButton(Component.literal("-"), COLOR_DANGER);
                removeButton.setOnClick(() -> {
                    config.arrowWhitelist.removeAll(idsForLabel);
                    Minecraft.getInstance().setScreen(new ModConfigScreen(null));
                });
                entry.add(removeButton, 255, 3, 18, 18);
            }

            listContainer.add(entry);
        }

        CustomScrollPanel scrollPanel = new CustomScrollPanel(listContainer);
        whitelistCard.add(scrollPanel, 5, 5, 290, 110);

        // Save Button
        FlatButton saveButton = new FlatButton(Component.literal("SAVE & CLOSE"), COLOR_ACCENT);
        saveButton.setOnClick(() -> {
            config.save();
            Minecraft.getInstance().setScreen(null);
        });
        root.add(saveButton, 10, 230, 300, 22);

        root.validate(this);
    }

    private ItemStack getItemStackFromId(String idStr) {
        String itemIdStr = idStr;
        String potionIdStr = null;
        if (idStr.contains("#")) {
            String[] parts = idStr.split("#");
            itemIdStr = parts[0];
            potionIdStr = parts[1];
        }

        Identifier id = Identifier.tryParse(itemIdStr);
        if (id != null) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            ItemStack stack = item.getDefaultInstance();

            if (potionIdStr != null) {
                Identifier pId = Identifier.tryParse(potionIdStr);
                if (pId != null) {
                    java.util.Optional<net.minecraft.core.Holder.Reference<Potion>> potionOpt = BuiltInRegistries.POTION.get(pId);
                    if (potionOpt.isPresent()) {
                        stack = PotionContents.createItemStack(item, potionOpt.get());
                    }
                }
            }
            return stack;
        }
        return null;
    }

    private WPlainPanel createCard(int width, int height) {
        WPlainPanel card = new WPlainPanel();
        card.setSize(width, height);
        card.setBackgroundPainter((context, x, y, panel) -> {
            ScreenDrawing.drawBeveledPanel(context, x, y, panel.getWidth(), panel.getHeight(), COLOR_CARD, COLOR_CARD,
                    COLOR_CARD);
        });
        return card;
    }

    @Override
    public void addPainters() {
        this.rootPanel.setBackgroundPainter((context, x, y, panel) -> {
            ScreenDrawing.drawGuiPanel(context, x, y, panel.getWidth(), panel.getHeight(), COLOR_BACKGROUND);
        });
    }

    public void openDialog(WPanel dialog) {
        if (currentDialog == null) {
            currentDialog = dialog;
            int dx = (root.getWidth() - dialog.getWidth()) / 2;
            int dy = (root.getHeight() - dialog.getHeight()) / 2;
            root.add(dialog, dx, dy, dialog.getWidth(), dialog.getHeight());
            root.validate(this);
        }
    }

    private class FlatButton extends WButton {
        private final int color;
        private boolean isDialogWidget = false;

        public FlatButton(Component label, int color) {
            super(label);
            this.color = color;
        }

        public FlatButton setDialogWidget(boolean dialogWidget) {
            isDialogWidget = dialogWidget;
            return this;
        }

        @Override
        public void paint(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY) {
            boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight())
                    && (isDialogWidget || currentDialog == null);
            int drawColor = isEnabled() ? (hovered ? lighten(color, 30) : color) : 0xFF444444;
            ScreenDrawing.drawGuiPanel(context, x, y, getWidth(), getHeight(), drawColor);

            int textX = (getWidth() - Minecraft.getInstance().font.width(getLabel())) / 2;
            int textY = (getHeight() - 8) / 2;
            ScreenDrawing.drawString(context, getLabel().getVisualOrderText(), HorizontalAlignment.LEFT, x + textX,
                    y + textY, getWidth(), COLOR_TEXT);
        }

        @Override
        public InputResult onClick(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
            if (!isDialogWidget && currentDialog != null)
                return InputResult.IGNORED;
            return super.onClick(click, doubled);
        }

        private int lighten(int color, int amount) {
            int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
            int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
            int b = Math.min(255, (color & 0xFF) + amount);
            return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }

    private class BlueToggleButton extends WToggleButton {
        public BlueToggleButton(Component label) {
            super(label);
        }

        @Override
        public void paint(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY) {
            boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight())
                    && currentDialog == null;
            int boxColor = getToggle() ? COLOR_ACCENT : (hovered ? 0xFF444444 : 0xFF333333);

            ScreenDrawing.drawBeveledPanel(context, x, y + 1, 18, 18, boxColor, boxColor, boxColor);
            if (getToggle()) {
                ScreenDrawing.drawString(context, "✔", HorizontalAlignment.CENTER, x + 1, y + 6, 16, COLOR_TEXT);
            }

            ScreenDrawing.drawString(context, label.getVisualOrderText(), HorizontalAlignment.LEFT, x + 24, y + 6,
                    getWidth() - 24, COLOR_TEXT);
        }

        @Override
        public InputResult onClick(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
            if (currentDialog != null)
                return InputResult.IGNORED;
            return super.onClick(click, doubled);
        }
    }

    private class BlueScrollBar extends WScrollBar {
        public BlueScrollBar(Axis axis) {
            super(axis);
        }

        @Override
        public WScrollBar setValue(int value) {
            super.setValue(value);
            if (parent != null) parent.layout();
            return this;
        }

        @Override
        public void paint(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY) {
            ScreenDrawing.coloredRect(context, x, y, width, height, 0x44FFFFFF);
            if (maxValue <= 0)
                return;

            boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
            int handleColor = sliding ? 0xFF888888 : (hovered ? 0xFFAAAAAA : 0xFF666666);

            ScreenDrawing.coloredRect(context, x + 1, y + 1 + getHandlePosition(), width - 2, getHandleSize(),
                    handleColor);
        }
    }

    private class CustomScrollPanel extends WPanel {
        private final WWidget widget;
        private final WScrollBar scrollBar;

        public CustomScrollPanel(WWidget widget) {
            this.widget = widget;
            this.scrollBar = new BlueScrollBar(Axis.VERTICAL);
            this.widget.setParent(this);
            this.scrollBar.setParent(this);
            this.children.add(widget);
            this.children.add(scrollBar);
        }

        @Override
        public void layout() {
            scrollBar.setSize(8, height);
            scrollBar.setLocation(width - 8, 0);
            scrollBar.setWindow(height);
            int max = Math.max(0, widget.getHeight() - height);
            scrollBar.setMaxValue(max);
            
            // Clamp current value to new max
            if (scrollBar.getValue() > max) {
                scrollBar.setValue(max);
            }

            boolean showScroll = scrollBar.getMaxValue() > 0;
            int widgetWidth = showScroll ? width - 10 : width;

            widget.setSize(widgetWidth, widget.getHeight());
            widget.setLocation(0, -scrollBar.getValue());
            if (widget instanceof WPanel)
                ((WPanel) widget).layout();
        }

        @Override
        public void paint(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY) {
            widget.setLocation(0, -scrollBar.getValue()); // Ensure position is always synced with scrollbar value
            
            boolean showScroll = scrollBar.getMaxValue() > 0;
            int clipWidth = showScroll ? width - 8 : width;

            context.enableScissor(x, y, x + clipWidth, y + height);
            widget.paint(context, x + widget.getX(), y + widget.getY(), mouseX - widget.getX(), mouseY - widget.getY());
            context.disableScissor();

            if (showScroll) {
                scrollBar.paint(context, x + scrollBar.getX(), y + scrollBar.getY(), mouseX - scrollBar.getX(),
                        mouseY - scrollBar.getY());
            }
        }

        @Override
        public InputResult onMouseScroll(int x, int y, double horizontalAmount, double verticalAmount) {
            if (scrollBar.getMaxValue() <= 0)
                return InputResult.IGNORED;
            return scrollBar.onMouseScroll(0, 0, horizontalAmount, verticalAmount);
        }
    }

    private class ArrowPicker extends WPlainPanel {
        private final java.util.function.Consumer<List<String>> callback;
        private final List<String> selected = new ArrayList<>();
        private final ClearableBox pickerList;
        private final CustomScrollPanel pScroll;
        private final List<ArrowGroup> allGroups = new ArrayList<>();

        private record ArrowGroup(String label, ItemStack displayStack, List<String> keys) {}

        public ArrowPicker(java.util.function.Consumer<List<String>> callback) {
            this.callback = callback;
            setSize(240, 240);
            setBackgroundPainter((context, x, y, panel) -> {
                ScreenDrawing.drawGuiPanel(context, x, y, panel.getWidth(), panel.getHeight(), 0xFF262626);
                ScreenDrawing.drawBeveledPanel(context, x, y, panel.getWidth(), panel.getHeight(), 0xFF666666,
                        0xFF222222, 0xFF666666);
            });

            WLabel pTitle = new WLabel(Component.literal("Select Arrows"), COLOR_ACCENT);
            pTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
            add(pTitle, 0, 8, 240, 15);

            WTextField searchField = new WTextField(Component.literal("Search..."));
            add(searchField, 10, 25, 220, 20);

            pickerList = new ClearableBox(Axis.VERTICAL);
            pickerList.setSpacing(2);

            // Fetch ALL base arrow items once
            List<Item> baseArrows = BuiltInRegistries.ITEM.stream()
                    .filter(item -> {
                        String path = BuiltInRegistries.ITEM.getKey(item).getPath().toLowerCase();
                        String name = item.getName(item.getDefaultInstance()).getString().toLowerCase();
                        return item instanceof ArrowItem || path.contains("arrow") || name.contains("arrow");
                    })
                    .distinct()
                    .collect(Collectors.toList());

            Map<String, ArrowGroup> groupedEntries = new LinkedHashMap<>();
            for (Item item : baseArrows) {
                if (item == Items.TIPPED_ARROW) {
                    for (Map.Entry<ResourceKey<Potion>, Potion> entry : BuiltInRegistries.POTION.entrySet()) {
                        ResourceKey<Potion> key = entry.getKey();
                        if (key.identifier().getPath().equals("empty")) continue;
                        
                        ItemStack stack = PotionContents.createItemStack(Items.TIPPED_ARROW, BuiltInRegistries.POTION.wrapAsHolder(entry.getValue()));
                        String uniqueKey = BuiltInRegistries.ITEM.getKey(item).toString() + "#" + key.identifier().toString();
                        String label = stack.getHoverName().getString();
                        groupedEntries.computeIfAbsent(label, l -> new ArrowGroup(l, stack, new ArrayList<>())).keys().add(uniqueKey);
                    }
                } else {
                    ItemStack stack = item.getDefaultInstance();
                    String uniqueKey = BuiltInRegistries.ITEM.getKey(item).toString();
                    String label = stack.getHoverName().getString();
                    groupedEntries.computeIfAbsent(label, l -> new ArrowGroup(l, stack, new ArrayList<>())).keys().add(uniqueKey);
                }
            }
            allGroups.addAll(groupedEntries.values());

            searchField.setChangedListener(this::updateFilter);

            pScroll = new CustomScrollPanel(pickerList);
            add(pScroll, 5, 50, 230, 155);

            updateFilter(""); // Initial populate

            FlatButton closeBtn = new FlatButton(Component.literal("CANCEL"), 0xFF666666).setDialogWidget(true);
            closeBtn.setOnClick(() -> {
                currentDialog = null;
                root.remove(this);
            });
            add(closeBtn, 10, 210, 220, 22);
        }

        private void updateFilter(String filter) {
            pickerList.clear();
            String lowerFilter = filter.toLowerCase();
            int count = 0;
            for (ArrowGroup group : allGroups) {
                if (group.label().toLowerCase().contains(lowerFilter)) {
                    addEntry(pickerList, group);
                    count++;
                }
            }
            pickerList.setSize(220, count * 26);
            pickerList.layout();
            pScroll.layout();
            pScroll.scrollBar.setValue(0); // Reset scroll on new search
        }

        private void addEntry(ClearableBox list, ArrowGroup group) {
            WPlainPanel entry = new WPlainPanel();
            entry.setSize(220, 24);

            WLabel label = new WLabel(group.displayStack().getHoverName(), COLOR_TEXT);
            entry.add(label, 30, 4, 150, 15);

            WItem icon = new WItem(group.displayStack());
            entry.add(icon, 5, 4, 16, 16);

            FlatButton selectBtn = new FlatButton(Component.literal("+"), COLOR_ACCENT).setDialogWidget(true);
            selectBtn.setOnClick(() -> {
                selected.addAll(group.keys());
                callback.accept(selected);
                currentDialog = null;
                root.remove(this);
            });
            entry.add(selectBtn, 195, 3, 18, 18);

            list.add(entry);
        }
    }

    private static class ClearableBox extends WBox {
        public ClearableBox(Axis axis) {
            super(axis);
        }
        public void clear() {
            this.children.clear();
        }
    }
}
