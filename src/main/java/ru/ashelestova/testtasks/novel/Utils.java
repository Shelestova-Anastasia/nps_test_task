package ru.ashelestova.testtasks.novel;

class Utils {
    static String parseId(String idLine){
        if(idLine == null || !idLine.startsWith("@")){
            throw new DataByIdException("Unexpected id format. Id is expected to start with @. Unparsed id = " + idLine);
        }

        return idLine.substring(1);
    }
}
