package com.ezinnovations.ezteleport.util;

import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;

public enum TeleportMessageKey {
    COUNTING("counting") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.counting();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.counting();
        }
    },
    CANCELLED_MOVE("cancelled-move") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.cancelledMove();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.cancelledMove();
        }
    },
    CANCELLED_DAMAGE("cancelled-damage") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.cancelledDamage();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.cancelledDamage();
        }
    },
    COOLDOWN("cooldown") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.cooldown();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.cooldown();
        }
    },
    SUCCESS("success") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.success();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.success();
        }
    },
    NO_PERMISSION("no-permission") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.noPermission();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.noPermission();
        }
    },
    INVALID_WORLD("invalid-world") {
        @Override
        public String resolve(TeleportCommandDefinition.Messages messages) {
            return messages.invalidWorld();
        }

        @Override
        public boolean enabled(TeleportCommandDefinition.Channel channel) {
            return channel.invalidWorld();
        }
    };

    private final String configKey;

    TeleportMessageKey(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }

    public abstract String resolve(TeleportCommandDefinition.Messages messages);

    public abstract boolean enabled(TeleportCommandDefinition.Channel channel);
}
