/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.room;

import androidx.annotation.RestrictTo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Repeatable annotation to be used by the user in specifying renamed columns between the old and
 * new versions of one database.
 *
 * @hide
 */
@Repeatable(RenameColumn.Entries.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public @interface RenameColumn {
    /**
     * Name of the table the renamed column is found in.
     *
     * @return Name of the table
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    String tableName();

    /**
     * Original name of the column to be renamed from.
     *
     * @return Name of the column in the previous version of the database.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    String originalColumnName();

    /**
     * New name of the column to be renamed to.
     *
     * @return Name of the column in the new version of the database.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    String newColumnName();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @interface Entries {
        RenameColumn[] value();
    }
}
