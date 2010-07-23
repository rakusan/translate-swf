/*
 * PlayerType.java
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
public enum PlayerType {
	/** The stand-alone Flash Player or Web Browser plugin. */
	DEFAULT("Default"),
	/** The Flash Lite 1.0 Player */
	FLASH_LITE_10("FlashLite10"),
	/** The Flash Lite 1.1 Player */
	FLASH_LITE_11("FlashLite11"),
	/** The Flash Lite 2.0 Player */
	FLASH_LITE_20("FlashLite20"),
	/** The Flash Lite 3.0 Player */
	FLASH_LITE_30("FlashLite30");


	/** Table used to convert names into PlayerTypes. */
    private static final Map<String, PlayerType> TABLE =
        new LinkedHashMap<String, PlayerType>();

    static {
        for (final PlayerType type : values()) {
            TABLE.put(type.value, type);
        }
    }

    public static PlayerType fromName(final String name) {
        return TABLE.get(name);
    }

    private final String value;

    private PlayerType(final String name) {
    	value = name;
    }

    public String getValue() {
    	return value;
    }

}
