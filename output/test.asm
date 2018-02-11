    DLabel       $print-format-string      
    DataC        37                        %% "%s"
    DataC        115                       
    DataC        0 
    PushI        0
    JumpFalse    falseLabel
    DLabel       -string-constant-2-       
    DataC        88                        %% "XXX"
    DataC        88                        
    DataC        88                        
    DataC        0                         
    PushD        -string-constant-2-       
    PushD        $print-format-string 
    Printf
    Jump		 endLabel
    Label        falseLabel
    DLabel       -string-constant-3-       
    DataC        89                        %% "YYY"
    DataC        89
    DataC        89                        
    DataC        0                         
    PushD        -string-constant-3-       
    PushD        $print-format-string 
    Printf
    Label		endLabel