// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//<auto-generated />

using System.Collections.Generic;
using System.Runtime.Serialization;

namespace Org.Apache.REEF.Tang.Implementations.ClassHierarchy.AvroDataContract
{
    /// <summary>
    /// Used to serialize and deserialize Avro record org.apache.reef.tang.implementation.avro.AvroNamedParameterNode.
    /// </summary>
    [DataContract(Namespace = "org.apache.reef.tang.implementation.avro")]
    [KnownType(typeof(List<string>))]
    public partial class AvroNamedParameterNode
    {
        private const string JsonSchema = @"{""type"":""record"",""name"":""org.apache.reef.tang.implementation.avro.AvroNamedParameterNode"",""fields"":[{""name"":""simpleArgClassName"",""type"":""string""},{""name"":""fullArgClassName"",""type"":""string""},{""name"":""isSet"",""type"":""boolean""},{""name"":""isList"",""type"":""boolean""},{""name"":""documentation"",""type"":[""null"",""string""]},{""name"":""shortName"",""type"":[""null"",""string""]},{""name"":""instanceDefault"",""type"":{""type"":""array"",""items"":""string""}}]}";

        /// <summary>
        /// Gets the schema.
        /// </summary>
        public static string Schema
        {
            get
            {
                return JsonSchema;
            }
        }
      
        /// <summary>
        /// Gets or sets the simpleArgClassName field.
        /// </summary>
        [DataMember]
        public string simpleArgClassName { get; set; }
              
        /// <summary>
        /// Gets or sets the fullArgClassName field.
        /// </summary>
        [DataMember]
        public string fullArgClassName { get; set; }
              
        /// <summary>
        /// Gets or sets the isSet field.
        /// </summary>
        [DataMember]
        public bool isSet { get; set; }
              
        /// <summary>
        /// Gets or sets the isList field.
        /// </summary>
        [DataMember]
        public bool isList { get; set; }
              
        /// <summary>
        /// Gets or sets the documentation field.
        /// </summary>
        [DataMember]
        public string documentation { get; set; }
              
        /// <summary>
        /// Gets or sets the shortName field.
        /// </summary>
        [DataMember]
        public string shortName { get; set; }
              
        /// <summary>
        /// Gets or sets the instanceDefault field.
        /// </summary>
        [DataMember]
        public List<string> instanceDefault { get; set; }
                
        /// <summary>
        /// Initializes a new instance of the <see cref="AvroNamedParameterNode"/> class.
        /// </summary>
        public AvroNamedParameterNode()
        {
            this.documentation = null;
            this.shortName = null;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="AvroNamedParameterNode"/> class.
        /// </summary>
        /// <param name="simpleArgClassName">The simpleArgClassName.</param>
        /// <param name="fullArgClassName">The fullArgClassName.</param>
        /// <param name="isSet">The isSet.</param>
        /// <param name="isList">The isList.</param>
        /// <param name="documentation">The documentation.</param>
        /// <param name="shortName">The shortName.</param>
        /// <param name="instanceDefault">The instanceDefault.</param>
        public AvroNamedParameterNode(string simpleArgClassName, string fullArgClassName, bool isSet, bool isList, string documentation, string shortName, List<string> instanceDefault)
        {
            this.simpleArgClassName = simpleArgClassName;
            this.fullArgClassName = fullArgClassName;
            this.isSet = isSet;
            this.isList = isList;
            this.documentation = documentation;
            this.shortName = shortName;
            this.instanceDefault = instanceDefault;
        }
    }
}