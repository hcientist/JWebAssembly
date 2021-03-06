/*
   Copyright 2018 - 2019 Volker Berlin (i-net software)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package de.inetsoftware.jwebassembly.module;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import de.inetsoftware.jwebassembly.wasm.AnyType;

/**
 * Manage the written and to write types (classes)
 * 
 * @author Volker Berlin
 */
public class TypeManager {

    private final Map<String, StructType> map = new LinkedHashMap<>();

    /**
     * Use the type in the output.
     * 
     * @param type
     *            the reference to a type
     * @param id
     *            the id in the type section of the wasm
     */
    void useType( StructType type, int id ) {
        type.code = id;
    }

    /**
     * Get the registered types in numeric order.
     * 
     * @return the types
     */
    @Nonnull
    Collection<StructType> getTypes() {
        return map.values();
    }

    /**
     * Get the StructType. If needed an instance is created.
     * 
     * @param name
     *            the type name
     * @return the struct type
     */
    public StructType valueOf( String name ) {
        StructType type = map.get( name );
        if( type == null ) {
            type = new StructType( name );
            map.put( name, type );
        }
        return type;
    }

    /**
     * A reference to a type.
     * 
     * @author Volker Berlin
     */
    static class StructType implements AnyType {

        private final String name;

        private int          code = Integer.MAX_VALUE;

        /**
         * Create a reference to type
         * 
         * @param name
         *            the Java class name
         */
        StructType( String name ) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCode() {
            return code;
        }

        /**
         * Get the name of the Java type
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "$" + name;
        }
    }
}
