/*
 * Profile.java
 * Translate
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.translate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PlayerType is used to identify the different run-time platforms that
 * support Flash content.
 */
public enum Profile {
	/** Profile for standard Flash Player, Actionscript 1, Flash Version 5. */
	DEFAULT_1_5("Default", 1, 5),
	/** Profile for standard Flash Player, Actionscript 1, Flash Version 6. */
	DEFAULT_1_6("Default", 1, 6),
	/** Profile for standard Flash Player, Actionscript 1, Flash Version 7. */
	DEFAULT_1_7("Default", 1, 7),
	/** Profile for standard Flash Player, Actionscript 1, Flash Version 8. */
	DEFAULT_1_8("Default", 1, 8),
	;

	/** Table used to convert names into PlayerTypes. */
    private static final Map<String, Profile> TABLE =
        new LinkedHashMap<String, Profile>();

    static {
        for (final Profile type : values()) {
            TABLE.put(type.toString(), type);
        }
    }

    public static Profile fromName(final String name) {
        return TABLE.get(name);
    }

    private final String player;
    private final int script;
    private final int flash;

    private Profile(final String platform, final int scriptVersion,
    		final int flashVersion) {
    	player = platform;
    	script = scriptVersion;
    	flash = flashVersion;
    }

    public String getPlayer() {
    	return player;
    }

    public int getScriptVersion() {
    	return script;
    }

    public int getFlashVersion() {
    	return flash;
    }
}
