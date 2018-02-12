        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%g"
        DataC        103                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-rational    
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-tab         
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        Label        $lowest-terms             
        DLabel       $return-address           
        DataZ        4                         
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    $$denominator-zero        
        DLabel       $denominator              
        DataZ        4                         
        PushD        $denominator              
        Exchange                               
        StoreI                                 
        DLabel       $numerator                
        DataZ        4                         
        PushD        $numerator                
        Exchange                               
        StoreI                                 
        DLabel       $a                        
        DataZ        4                         
        PushD        $numerator                
        LoadI                                  
        PushD        $a                        
        Exchange                               
        StoreI                                 
        DLabel       $b                        
        DataZ        4                         
        PushD        $denominator              
        LoadI                                  
        PushD        $b                        
        Exchange                               
        StoreI                                 
        Label        $gcd-loop                 
        PushD        $b                        
        LoadI                                  
        JumpFalse    $gcd-end                  
        PushD        $a                        
        LoadI                                  
        PushD        $b                        
        LoadI                                  
        Remainder                              
        PushD        $b                        
        LoadI                                  
        PushD        $a                        
        Exchange                               
        StoreI                                 
        PushD        $b                        
        Exchange                               
        StoreI                                 
        Jump         $gcd-loop                 
        Label        $gcd-end                  
        PushD        $numerator                
        LoadI                                  
        PushD        $a                        
        LoadI                                  
        Divide                                 
        PushD        $denominator              
        LoadI                                  
        PushD        $a                        
        LoadI                                  
        Divide                                 
        PushD        $return-address           
        LoadI                                  
        Return                                 
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "float divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-denominator-zero  
        DataC        100                       %% "denominator zero"
        DataC        101                       
        DataC        110                       
        DataC        111                       
        DataC        109                       
        DataC        105                       
        DataC        110                       
        DataC        97                        
        DataC        116                       
        DataC        111                       
        DataC        114                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$denominator-zero        
        PushD        $errors-denominator-zero  
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        8                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        PushI        10                        
        PushI        15                        
        Call         $lowest-terms             
        StoreI                                 
        Halt                                   
