/*
 * Copyright 2018 - 2019 Volker Berlin (i-net software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.inetsoftware.jwebassembly.module;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.inetsoftware.classparser.Member;
import de.inetsoftware.jwebassembly.module.WasmInstruction.Type;
import de.inetsoftware.jwebassembly.wasm.AnyType;
import de.inetsoftware.jwebassembly.wasm.ArrayOperator;
import de.inetsoftware.jwebassembly.wasm.NumericOperator;
import de.inetsoftware.jwebassembly.wasm.StructOperator;
import de.inetsoftware.jwebassembly.wasm.ValueType;
import de.inetsoftware.jwebassembly.wasm.WasmBlockOperator;

/**
 * Base class for Code Building.
 *
 * @author Volker Berlin
 */
public abstract class WasmCodeBuilder {

    private final LocaleVariableManager localVariables = new LocaleVariableManager();

    private final List<WasmInstruction> instructions   = new ArrayList<>();

    private TypeManager                 types;

    /**
     * Get the list of instructions
     * 
     * @return the list
     */
    List<WasmInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Check if the last instruction is a return instruction
     * 
     * @return true, if a return
     */
    boolean isEndsWithReturn() {
        WasmInstruction instr = instructions.get( instructions.size() - 1 );
        if( instr.getType() == Type.Block ) {
            return ((WasmBlockInstruction)instr).getOperation() == WasmBlockOperator.RETURN;
        }
        return false;
    }

    /**
     * Initialize the code builder;
     * 
     * @param types
     *            the type manager
     */
    void init( TypeManager types ) {
        this.types = types;
    }

    /**
     * Get the data types of the local variables. The value is only valid until the next call.
     * 
     * @param paramCount
     *            the count of method parameter which should be exclude
     * @return the reused list with fresh values
     */
    List<AnyType> getLocalTypes( int paramCount ) {
        return localVariables.getLocalTypes( paramCount );
    }

    /**
     * Reset the code builder.
     */
    protected void reset() {
        instructions.clear();
        localVariables.reset();
    }

    /**
     * Calculate the index of the variables
     */
    protected void calculateVariables() {
        localVariables.calculate();
    }

    /**
     * Create a WasmLoadStoreInstruction.
     * 
     * @param valueType
     *            the value type
     * @param load
     *            true: if load
     * @param javaIdx
     *            the memory/slot index of the variable in Java byte code
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    @Nonnull
    protected void addLoadStoreInstruction( AnyType valueType, boolean load, @Nonnegative int javaIdx, int javaCodePos, int lineNumber ) {
        localVariables.use( valueType, javaIdx );
        instructions.add( new WasmLoadStoreInstruction( load, javaIdx, localVariables, javaCodePos, lineNumber ) );
    }

    /**
     * Create a WasmLoadStoreInstruction get_local/set_local.
     * 
     * @param load
     *            true: if load
     * @param wasmIdx
     *            the index of the variable
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    @Nonnull
    protected void addLocalInstruction( boolean load, @Nonnegative int wasmIdx, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmLocalInstruction( load, wasmIdx, javaCodePos, lineNumber ) );
    }

    /**
     * Add a global instruction
     * 
     * @param load
     *            true: if load
     * @param ref
     *            reference to a static field
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addGlobalInstruction( boolean load, Member ref, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmGlobalInstruction( load, ref, javaCodePos, lineNumber ) );
    }

    /**
     * Add a constant instruction.
     * 
     * @param value
     *            the value
     * @param valueType
     *            the value type
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addConstInstruction( Number value, ValueType valueType, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmConstInstruction( value, valueType, javaCodePos, lineNumber ) );
    }

    /**
     * Add a constant instruction with unknown value type.
     * 
     * @param value
     *            the value
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addConstInstruction( Number value, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmConstInstruction( value, javaCodePos, lineNumber ) );
    }

    /**
     * Add a numeric operation instruction
     * 
     * @param numOp
     *            the operation
     * @param valueType
     *            the value type
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addNumericInstruction( @Nullable NumericOperator numOp, @Nullable ValueType valueType, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmNumericInstruction( numOp, valueType, javaCodePos, lineNumber ) );
    }

    /**
     * Add a value convert/cast instruction.
     * 
     * @param conversion
     *            the conversion
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addConvertInstruction( ValueTypeConvertion conversion, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmConvertInstruction( conversion, javaCodePos, lineNumber ) );
    }

    /**
     * Add a static function call.
     * 
     * @param name
     *            the function name that should be called
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addCallInstruction( FunctionName name, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmCallInstruction( name, javaCodePos, lineNumber ) );
    }

    /**
     * Add a block operation.
     * 
     * @param op
     *            the operation
     * @param data
     *            extra data for some operations
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addBlockInstruction( WasmBlockOperator op, @Nullable Object data, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmBlockInstruction( op, data, javaCodePos, lineNumber ) );
    }

    /**
     * Add a no operation to the instruction list as marker on the code position. This instruction will not be write to
     * the output.
     * 
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addNopInstruction( int javaCodePos, int lineNumber ) {
        instructions.add( new WasmNopInstruction( javaCodePos, lineNumber ) );
    }

    /**
     * Add an array operation to the instruction list as marker on the code position.
     * 
     * @param op
     *            the operation
     * @param type
     *            the array type
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addArrayInstruction( ArrayOperator op, AnyType type, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmArrayInstruction( op, type, javaCodePos, lineNumber ) );
    }

    /**
     * Add an array operation to the instruction list as marker on the code position.
     * 
     * @param op
     *            the operation
     * @param typeName
     *            the type name
     * @param fieldName
     *            the name of field if needed for the operation
     * @param javaCodePos
     *            the code position/offset in the Java method
     * @param lineNumber
     *            the line number in the Java source code
     */
    protected void addStructInstruction( StructOperator op, @Nullable String typeName, @Nullable String fieldName, int javaCodePos, int lineNumber ) {
        instructions.add( new WasmStructInstruction( op, typeName == null ? null : types.valueOf( typeName ), fieldName, javaCodePos, lineNumber ) );
    }
}
