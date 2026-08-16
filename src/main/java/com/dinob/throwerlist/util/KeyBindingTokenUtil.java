package com.dinob.throwerlist.util;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class KeyBindingTokenUtil {
    private KeyBindingTokenUtil() {}

    public static int parseKeyCode(String token) {
        if (token == null || token.isBlank()) {
            return -1;
        }

        String clean = token.trim();
        if (clean.equalsIgnoreCase("NONE")) {
            return -1;
        }

        if (clean.length() == 1) {
            char c = clean.charAt(0);
            return switch (c) {
                case '0', ')' -> GLFW.GLFW_KEY_0;
                case '1', '!' -> GLFW.GLFW_KEY_1;
                case '2', '@' -> GLFW.GLFW_KEY_2;
                case '3', '#' -> GLFW.GLFW_KEY_3;
                case '4', '$' -> GLFW.GLFW_KEY_4;
                case '5', '%' -> GLFW.GLFW_KEY_5;
                case '6', '^' -> GLFW.GLFW_KEY_6;
                case '7', '&' -> GLFW.GLFW_KEY_7;
                case '8', '*' -> GLFW.GLFW_KEY_8;
                case '9', '(' -> GLFW.GLFW_KEY_9;
                case '-' -> GLFW.GLFW_KEY_MINUS;
                case '=', '+' -> GLFW.GLFW_KEY_EQUAL;
                case '[' -> GLFW.GLFW_KEY_LEFT_BRACKET;
                case ']' -> GLFW.GLFW_KEY_RIGHT_BRACKET;
                case '\\' -> GLFW.GLFW_KEY_BACKSLASH;
                case ';' -> GLFW.GLFW_KEY_SEMICOLON;
                case '\'' -> GLFW.GLFW_KEY_APOSTROPHE;
                case ',' -> GLFW.GLFW_KEY_COMMA;
                case '.' -> GLFW.GLFW_KEY_PERIOD;
                case '/' -> GLFW.GLFW_KEY_SLASH;
                case '`', '~' -> GLFW.GLFW_KEY_GRAVE_ACCENT;
                default -> {
                    char upper = Character.toUpperCase(c);
                    if (upper >= 'A' && upper <= 'Z') yield GLFW.GLFW_KEY_A + (upper - 'A');
                    if (upper >= '0' && upper <= '9') yield GLFW.GLFW_KEY_0 + (upper - '0');
                    yield -1;
                }
            };
        }

        String upper = clean.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (upper.startsWith("F")) {
            String numPart = upper.substring(1);
            try {
                int functionKey = Integer.parseInt(numPart);
                if (functionKey >= 1 && functionKey <= 25) {
                    return GLFW.GLFW_KEY_F1 + (functionKey - 1);
                }
            } catch (NumberFormatException ignored) {}
        }

        return switch (upper) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER;
            case "ESC", "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "BACKSPACE", "BKSP" -> GLFW.GLFW_KEY_BACKSPACE;
            case "DELETE", "DEL" -> GLFW.GLFW_KEY_DELETE;
            case "INSERT", "INS" -> GLFW.GLFW_KEY_INSERT;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "PAGE_UP", "PGUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGE_DOWN", "PGDN", "PGDOWN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "UP", "UP_ARROW" -> GLFW.GLFW_KEY_UP;
            case "DOWN", "DOWN_ARROW" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT", "LEFT_ARROW" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT", "RIGHT_ARROW" -> GLFW.GLFW_KEY_RIGHT;
            case "LEFT_SHIFT", "LSHIFT", "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RIGHT_SHIFT", "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LEFT_CTRL", "LCTRL", "CTRL", "CONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RIGHT_CTRL", "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LEFT_ALT", "LALT", "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RIGHT_ALT", "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "CAPS_LOCK" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "NUM_LOCK" -> GLFW.GLFW_KEY_NUM_LOCK;
            case "SCROLL_LOCK" -> GLFW.GLFW_KEY_SCROLL_LOCK;
            case "PRINT_SCREEN", "PRTSC" -> GLFW.GLFW_KEY_PRINT_SCREEN;
            case "PAUSE", "BREAK" -> GLFW.GLFW_KEY_PAUSE;
            case "MENU", "APPLICATION" -> GLFW.GLFW_KEY_MENU;
            case "MINUS" -> GLFW.GLFW_KEY_MINUS;
            case "EQUALS", "PLUS" -> GLFW.GLFW_KEY_EQUAL;
            case "LEFT_BRACKET", "LBRACKET" -> GLFW.GLFW_KEY_LEFT_BRACKET;
            case "RIGHT_BRACKET", "RBRACKET" -> GLFW.GLFW_KEY_RIGHT_BRACKET;
            case "BACKSLASH" -> GLFW.GLFW_KEY_BACKSLASH;
            case "SEMICOLON" -> GLFW.GLFW_KEY_SEMICOLON;
            case "APOSTROPHE", "QUOTE" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "COMMA" -> GLFW.GLFW_KEY_COMMA;
            case "PERIOD", "DOT" -> GLFW.GLFW_KEY_PERIOD;
            case "SLASH" -> GLFW.GLFW_KEY_SLASH;
            case "GRAVE", "GRAVE_ACCENT", "BACKTICK", "TILDE" -> GLFW.GLFW_KEY_GRAVE_ACCENT;
            case "NUMPAD_0" -> GLFW.GLFW_KEY_KP_0;
            case "NUMPAD_1" -> GLFW.GLFW_KEY_KP_1;
            case "NUMPAD_2" -> GLFW.GLFW_KEY_KP_2;
            case "NUMPAD_3" -> GLFW.GLFW_KEY_KP_3;
            case "NUMPAD_4" -> GLFW.GLFW_KEY_KP_4;
            case "NUMPAD_5" -> GLFW.GLFW_KEY_KP_5;
            case "NUMPAD_6" -> GLFW.GLFW_KEY_KP_6;
            case "NUMPAD_7" -> GLFW.GLFW_KEY_KP_7;
            case "NUMPAD_8" -> GLFW.GLFW_KEY_KP_8;
            case "NUMPAD_9" -> GLFW.GLFW_KEY_KP_9;
            case "NUMPAD_ADD" -> GLFW.GLFW_KEY_KP_ADD;
            case "NUMPAD_SUBTRACT" -> GLFW.GLFW_KEY_KP_SUBTRACT;
            case "NUMPAD_MULTIPLY" -> GLFW.GLFW_KEY_KP_MULTIPLY;
            case "NUMPAD_DIVIDE" -> GLFW.GLFW_KEY_KP_DIVIDE;
            case "NUMPAD_DECIMAL" -> GLFW.GLFW_KEY_KP_DECIMAL;
            case "NUMPAD_ENTER" -> GLFW.GLFW_KEY_KP_ENTER;
            default -> -1;
        };
    }

    public static String keyCodeToToken(int keyCode) {
        if (keyCode < 0) return "NONE";
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + (keyCode - GLFW.GLFW_KEY_A)));
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + (keyCode - GLFW.GLFW_KEY_0)));
        }
        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESCAPE";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGE_UP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGE_DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LEFT_SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RIGHT_SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LEFT_CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RIGHT_CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LEFT_ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RIGHT_ALT";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS_LOCK";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUM_LOCK";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLL_LOCK";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRINT_SCREEN";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_MENU -> "MENU";
            case GLFW.GLFW_KEY_MINUS -> "MINUS";
            case GLFW.GLFW_KEY_EQUAL -> "EQUALS";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "LEFT_BRACKET";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "RIGHT_BRACKET";
            case GLFW.GLFW_KEY_BACKSLASH -> "BACKSLASH";
            case GLFW.GLFW_KEY_SEMICOLON -> "SEMICOLON";
            case GLFW.GLFW_KEY_APOSTROPHE -> "APOSTROPHE";
            case GLFW.GLFW_KEY_COMMA -> "COMMA";
            case GLFW.GLFW_KEY_PERIOD -> "PERIOD";
            case GLFW.GLFW_KEY_SLASH -> "SLASH";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "GRAVE";
            case GLFW.GLFW_KEY_KP_0 -> "NUMPAD_0";
            case GLFW.GLFW_KEY_KP_1 -> "NUMPAD_1";
            case GLFW.GLFW_KEY_KP_2 -> "NUMPAD_2";
            case GLFW.GLFW_KEY_KP_3 -> "NUMPAD_3";
            case GLFW.GLFW_KEY_KP_4 -> "NUMPAD_4";
            case GLFW.GLFW_KEY_KP_5 -> "NUMPAD_5";
            case GLFW.GLFW_KEY_KP_6 -> "NUMPAD_6";
            case GLFW.GLFW_KEY_KP_7 -> "NUMPAD_7";
            case GLFW.GLFW_KEY_KP_8 -> "NUMPAD_8";
            case GLFW.GLFW_KEY_KP_9 -> "NUMPAD_9";
            case GLFW.GLFW_KEY_KP_ADD -> "NUMPAD_ADD";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "NUMPAD_SUBTRACT";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "NUMPAD_MULTIPLY";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "NUMPAD_DIVIDE";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "NUMPAD_DECIMAL";
            case GLFW.GLFW_KEY_KP_ENTER -> "NUMPAD_ENTER";
            default -> "";
        };
    }

    public static String toDisplayName(String token) {
        int keyCode = parseKeyCode(token);
        if (keyCode < 0) return "None";

        return switch (keyCode) {
            case GLFW.GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock";
            case GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll Lock";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            default -> {
                String mapped = keyCodeToToken(keyCode);
                if (mapped.isBlank()) yield "Unknown";
                if (mapped.startsWith("NUMPAD_")) yield mapped.replace("NUMPAD_", "Numpad ").replace('_', ' ');
                yield switch (mapped) {
                    case "ESCAPE" -> "Escape";
                    case "BACKSPACE" -> "Backspace";
                    case "DELETE" -> "Delete";
                    case "INSERT" -> "Insert";
                    case "HOME" -> "Home";
                    case "END" -> "End";
                    case "UP" -> "Up";
                    case "DOWN" -> "Down";
                    case "LEFT" -> "Left";
                    case "RIGHT" -> "Right";
                    case "ENTER" -> "Enter";
                    case "SPACE" -> "Space";
                    case "TAB" -> "Tab";
                    case "PAUSE" -> "Pause";
                    case "MENU" -> "Menu";
                    default -> mapped;
                };
            }
        };
    }
}