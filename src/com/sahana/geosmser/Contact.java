/*******************************************************************************
 * Copyright 2011 Cai Fang, Ye
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sahana.geosmser;

public class Contact implements Comparable{
    private String Name;
    private String number;
 
    public String getName() {
        return Name;
    }
 
    public void setName(String Name) {
        this.Name = Name;
    }
 
    public String getNumber() {
        return number;
    }
 
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stub
        Contact newCont = (Contact)arg0;
        return this.Name.compareTo(newCont.getName());
    }

   
}
