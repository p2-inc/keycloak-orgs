import { customAlphabet } from "nanoid";
import { alphanumeric } from "nanoid-dictionary";

export const generateId = () => customAlphabet(alphanumeric, 6)();
