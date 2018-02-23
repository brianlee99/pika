        Label        -mem-manager-initialize   
        DLabel       $heap-start-ptr           
        DataZ        4                         
        DLabel       $heap-after-ptr           
        DataZ        4                         
        DLabel       $heap-first-free          
        DataZ        4                         
        DLabel       $mmgr-newblock-block      
        DataZ        4                         
        DLabel       $mmgr-newblock-size       
        DataZ        4                         
        PushD        $heap-memory              
        Duplicate                              
        PushD        $heap-start-ptr           
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
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
        DLabel       $errors-rat-divide-by-zero 
        DataC        114                       %% "rational divide by zero"
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        111                       
        DataC        110                       
        DataC        97                        
        DataC        108                       
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
        Label        $$r-divide-by-zero        
        PushD        $errors-rat-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-negative-length-arr 
        DataC        110                       %% "negative length array"
        DataC        101                       
        DataC        103                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        118                       
        DataC        101                       
        DataC        32                        
        DataC        108                       
        DataC        101                       
        DataC        110                       
        DataC        103                       
        DataC        116                       
        DataC        104                       
        DataC        32                        
        DataC        97                        
        DataC        114                       
        DataC        114                       
        DataC        97                        
        DataC        121                       
        DataC        0                         
        Label        $$negative-length-arr     
        PushD        $errors-negative-length-arr 
        Jump         $$general-runtime-error   
        DLabel       $errors-index-out-of-bounds 
        DataC        105                       %% "index out of bounds"
        DataC        110                       
        DataC        100                       
        DataC        101                       
        DataC        120                       
        DataC        32                        
        DataC        111                       
        DataC        117                       
        DataC        116                       
        DataC        32                        
        DataC        111                       
        DataC        102                       
        DataC        32                        
        DataC        98                        
        DataC        111                       
        DataC        117                       
        DataC        110                       
        DataC        100                       
        DataC        115                       
        DataC        0                         
        Label        $$index-out-of-bounds     
        PushD        $errors-index-out-of-bounds 
        Jump         $$general-runtime-error   
        DLabel       $errors-null-arr          
        DataC        110                       %% "null array"
        DataC        117                       
        DataC        108                       
        DataC        108                       
        DataC        32                        
        DataC        97                        
        DataC        114                       
        DataC        114                       
        DataC        97                        
        DataC        121                       
        DataC        0                         
        Label        $$null-array              
        PushD        $errors-null-arr          
        Jump         $$general-runtime-error   
        DLabel       $return-address           
        DataZ        4                         
        DLabel       $numerator-1              
        DataZ        4                         
        DLabel       $numerator-2              
        DataZ        4                         
        DLabel       $denominator-1            
        DataZ        4                         
        DLabel       $denominator-2            
        DataZ        4                         
        DLabel       $quotient                 
        DataZ        4                         
        DLabel       $remainder                
        DataZ        4                         
        DLabel       $express-over-denominator 
        DataZ        4                         
        DLabel       $record-creation-temp     
        DataZ        4                         
        DLabel       $array-datasize-temp      
        DataZ        4                         
        DLabel       $a-indexing-array         
        DataZ        4                         
        DLabel       $a-indexing-index         
        DataZ        4                         
        DLabel       $string-len-temp          
        DataZ        4                         
        DLabel       $clear-n-bytes-offset-temp 
        DataZ        4                         
        DLabel       $printf-arr-base          
        DataZ        4                         
        DLabel       $printf-arr-length        
        DataZ        4                         
        DLabel       $printf-arr-i             
        DataZ        4                         
        DLabel       $pop-arr-addr-temp        
        DataZ        4                         
        Label        $lowest-terms             
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        DataZ        4                         
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        DataZ        4                         
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        DLabel       $a                        
        DataZ        4                         
        PushD        $numerator-1              
        LoadI                                  
        PushD        $a                        
        Exchange                               
        StoreI                                 
        DLabel       $b                        
        DataZ        4                         
        PushD        $denominator-1            
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
        PushD        $numerator-1              
        LoadI                                  
        PushD        $a                        
        LoadI                                  
        Divide                                 
        PushD        $denominator-1            
        LoadI                                  
        PushD        $a                        
        LoadI                                  
        Divide                                 
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $rational-add             
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        PushD        $denominator-2            
        Exchange                               
        StoreI                                 
        PushD        $numerator-2              
        Exchange                               
        StoreI                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $numerator-2              
        LoadI                                  
        PushD        $denominator-1            
        LoadI                                  
        Multiply                               
        Add                                    
        PushD        $denominator-1            
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $rational-subtract        
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        PushD        $denominator-2            
        Exchange                               
        StoreI                                 
        PushD        $numerator-2              
        Exchange                               
        StoreI                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $numerator-2              
        LoadI                                  
        PushD        $denominator-1            
        LoadI                                  
        Multiply                               
        Subtract                               
        PushD        $denominator-1            
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $rational-multiply        
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        PushD        $denominator-2            
        Exchange                               
        StoreI                                 
        PushD        $numerator-2              
        Exchange                               
        StoreI                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $numerator-2              
        LoadI                                  
        Multiply                               
        PushD        $denominator-1            
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $rational-divide          
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        PushD        $denominator-2            
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    $$r-divide-by-zero        
        PushD        $numerator-2              
        Exchange                               
        StoreI                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $numerator-2              
        LoadI                                  
        Multiply                               
        PushD        $denominator-1            
        LoadI                                  
        PushD        $denominator-2            
        LoadI                                  
        Multiply                               
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $printf-rational          
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        JumpNeg      $rat-print-negative-numerator 
        Jump         $rat-print-check-denom-negative 
        Label        $rat-print-negative-numerator 
        PushI        45                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $numerator-1              
        LoadI                                  
        Negate                                 
        PushD        $numerator-1              
        Exchange                               
        StoreI                                 
        Jump         $rat-print-calculate-quotient 
        Label        $rat-print-check-denom-negative 
        PushD        $denominator-1            
        LoadI                                  
        JumpNeg      $rat-print-negative-denominator 
        Jump         $rat-print-calculate-quotient 
        Label        $rat-print-negative-denominator 
        PushI        45                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $denominator-1            
        LoadI                                  
        Negate                                 
        PushD        $denominator-1            
        Exchange                               
        StoreI                                 
        Label        $rat-print-calculate-quotient 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $denominator-1            
        LoadI                                  
        Divide                                 
        PushD        $quotient                 
        Exchange                               
        StoreI                                 
        PushD        $numerator-1              
        LoadI                                  
        PushD        $denominator-1            
        LoadI                                  
        Remainder                              
        PushD        $remainder                
        Exchange                               
        StoreI                                 
        PushD        $quotient                 
        LoadI                                  
        PushD        $remainder                
        LoadI                                  
        BTOr                                   
        JumpTrue     $rat-print-check-leading-number 
        PushI        0                         
        PushD        $print-format-integer     
        Printf                                 
        Jump         $rat-print-end            
        Label        $rat-print-check-leading-number 
        PushD        $quotient                 
        LoadI                                  
        JumpFalse    $rat-print-check-fraction 
        PushD        $quotient                 
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        Label        $rat-print-check-fraction 
        PushD        $remainder                
        LoadI                                  
        JumpFalse    $rat-print-end            
        PushI        95                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $remainder                
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushI        47                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $denominator-1            
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        Label        $rat-print-end            
        PushD        $return-address           
        LoadI                                  
        Return                                 
        Label        $clear-n-bytes            
        PushD        $return-address           
        Exchange                               
        StoreI                                 
        Label        $clear-n-bytes-loop       
        Duplicate                              
        JumpNeg      $clear-n-bytes-end        
        PushI        1                         
        Subtract                               
        PushD        $clear-n-bytes-offset-temp 
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushD        $clear-n-bytes-offset-temp 
        LoadI                                  
        Add                                    
        PushI        0                         
        StoreC                                 
        PushD        $clear-n-bytes-offset-temp 
        LoadI                                  
        Jump         $clear-n-bytes-loop       
        Label        $clear-n-bytes-end        
        Pop                                    
        Pop                                    
        PushD        $return-address           
        LoadI                                  
        Return                                 
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        4                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        PushI        1                         
        Duplicate                              
        JumpNeg      $$negative-length-arr     
        Duplicate                              
        PushI        4                         
        Multiply                               
        Duplicate                              
        PushD        $array-datasize-temp      
        Exchange                               
        StoreI                                 
        PushI        16                        
        Add                                    
        Call         -mem-manager-allocate     
        PushD        $record-creation-temp     
        Exchange                               
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        0                         
        Add                                    
        PushI        7                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        4                         
        Add                                    
        PushI        2                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        16                        
        Add                                    
        PushD        $array-datasize-temp      
        LoadI                                  
        Call         $clear-n-bytes            
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        8                         
        Add                                    
        PushI        4                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        12                        
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        1                         
        Duplicate                              
        JumpNeg      $$negative-length-arr     
        Duplicate                              
        PushI        4                         
        Multiply                               
        Duplicate                              
        PushD        $array-datasize-temp      
        Exchange                               
        StoreI                                 
        PushI        16                        
        Add                                    
        Call         -mem-manager-allocate     
        PushD        $record-creation-temp     
        Exchange                               
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        0                         
        Add                                    
        PushI        7                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        4                         
        Add                                    
        PushI        2                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        16                        
        Add                                    
        PushD        $array-datasize-temp      
        LoadI                                  
        Call         $clear-n-bytes            
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        8                         
        Add                                    
        PushI        4                         
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        12                        
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        1                         
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        16                        
        Add                                    
        PushI        0                         
        Add                                    
        PushD        $pop-arr-addr-temp        
        Exchange                               
        StoreI                                 
        PushD        $pop-arr-addr-temp        
        LoadI                                  
        Exchange                               
        StoreI                                 
        PushD        $record-creation-temp     
        LoadI                                  
        PushI        16                        
        Add                                    
        PushI        0                         
        Add                                    
        PushD        $pop-arr-addr-temp        
        Exchange                               
        StoreI                                 
        PushD        $pop-arr-addr-temp        
        LoadI                                  
        Exchange                               
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% x
        LoadI                                  
        PushI        91                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $printf-arr-base          
        Exchange                               
        StoreI                                 
        PushD        $printf-arr-base          
        LoadI                                  
        PushI        12                        
        Add                                    
        LoadI                                  
        PushD        $printf-arr-length        
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $printf-arr-i             
        Exchange                               
        StoreI                                 
        Label        -print-array-1-loop-body  
        PushD        $printf-arr-i             
        LoadI                                  
        PushD        $printf-arr-length        
        LoadI                                  
        Subtract                               
        JumpFalse    -print-array-1-loop-end   
        PushD        $printf-arr-base          
        LoadI                                  
        PushI        16                        
        Add                                    
        PushD        $printf-arr-i             
        LoadI                                  
        PushI        4                         
        Multiply                               
        Add                                    
        LoadI                                  
        PushI        91                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $printf-arr-base          
        Exchange                               
        StoreI                                 
        PushD        $printf-arr-base          
        LoadI                                  
        PushI        12                        
        Add                                    
        LoadI                                  
        PushD        $printf-arr-length        
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $printf-arr-i             
        Exchange                               
        StoreI                                 
        Label        -print-array-2-loop-body  
        PushD        $printf-arr-i             
        LoadI                                  
        PushD        $printf-arr-length        
        LoadI                                  
        Subtract                               
        JumpFalse    -print-array-2-loop-end   
        PushD        $printf-arr-base          
        LoadI                                  
        PushI        16                        
        Add                                    
        PushD        $printf-arr-i             
        LoadI                                  
        PushI        4                         
        Multiply                               
        Add                                    
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $printf-arr-i             
        LoadI                                  
        PushI        1                         
        Add                                    
        PushD        $printf-arr-i             
        Exchange                               
        StoreI                                 
        PushD        $printf-arr-i             
        LoadI                                  
        PushD        $printf-arr-length        
        LoadI                                  
        Subtract                               
        JumpFalse    -print-array-2-loop-end   
        PushI        44                        
        PushD        $print-format-character   
        Printf                                 
        PushI        32                        
        PushD        $print-format-character   
        Printf                                 
        Jump         -print-array-2-loop-body  
        Label        -print-array-2-loop-end   
        PushI        93                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $printf-arr-i             
        LoadI                                  
        PushI        1                         
        Add                                    
        PushD        $printf-arr-i             
        Exchange                               
        StoreI                                 
        PushD        $printf-arr-i             
        LoadI                                  
        PushD        $printf-arr-length        
        LoadI                                  
        Subtract                               
        JumpFalse    -print-array-1-loop-end   
        PushI        44                        
        PushD        $print-format-character   
        Printf                                 
        PushI        32                        
        PushD        $print-format-character   
        Printf                                 
        Jump         -print-array-1-loop-body  
        Label        -print-array-1-loop-end   
        PushI        93                        
        PushD        $print-format-character   
        Printf                                 
        Halt                                   
        Label        -mem-manager-make-tags    
        DLabel       $mmgr-tags-size           
        DataZ        4                         
        DLabel       $mmgr-tags-start          
        DataZ        4                         
        DLabel       $mmgr-tags-available      
        DataZ        4                         
        DLabel       $mmgr-tags-nextptr        
        DataZ        4                         
        DLabel       $mmgr-tags-prevptr        
        DataZ        4                         
        DLabel       $mmgr-tags-return         
        DataZ        4                         
        PushD        $mmgr-tags-return         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-size           
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-start          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-available      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-nextptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-nextptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-return         
        LoadI                                  
        Return                                 
        Label        -mem-manager-one-tag      
        DLabel       $mmgr-onetag-return       
        DataZ        4                         
        DLabel       $mmgr-onetag-location     
        DataZ        4                         
        DLabel       $mmgr-onetag-available    
        DataZ        4                         
        DLabel       $mmgr-onetag-size         
        DataZ        4                         
        DLabel       $mmgr-onetag-pointer      
        DataZ        4                         
        PushD        $mmgr-onetag-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        4                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-onetag-return       
        LoadI                                  
        Return                                 
        Label        -mem-manager-allocate     
        DLabel       $mmgr-alloc-return        
        DataZ        4                         
        DLabel       $mmgr-alloc-size          
        DataZ        4                         
        DLabel       $mmgr-alloc-current-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-size 
        DataZ        4                         
        PushD        $mmgr-alloc-return        
        Exchange                               
        StoreI                                 
        PushI        18                        
        Add                                    
        PushD        $mmgr-alloc-size          
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-process-current 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        JumpFalse    -mmgr-alloc-no-block-works 
        Label        -mmgr-alloc-test-block    
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        1                         
        Add                                    
        JumpPos      -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Jump         -mmgr-alloc-process-current 
        Label        -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Call         -mem-manager-remove-block 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        26                        
        Subtract                               
        JumpNeg      -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Add                                    
        PushD        $mmgr-alloc-remainder-block 
        Exchange                               
        StoreI                                 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        Exchange                               
        Subtract                               
        PushD        $mmgr-alloc-remainder-size 
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushI        0                         
        PushI        0                         
        PushI        1                         
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushD        $mmgr-alloc-remainder-size 
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        Call         -mem-manager-deallocate   
        Jump         -mmgr-alloc-return-userblock 
        Label        -mmgr-alloc-no-block-works 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-newblock-size       
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        LoadI                                  
        PushD        $mmgr-newblock-block      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-newblock-size       
        LoadI                                  
        PushD        $heap-after-ptr           
        LoadI                                  
        Add                                    
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-newblock-size       
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        PushD        $mmgr-alloc-return        
        LoadI                                  
        Return                                 
        Label        -mem-manager-deallocate   
        DLabel       $mmgr-dealloc-return      
        DataZ        4                         
        DLabel       $mmgr-dealloc-block       
        DataZ        4                         
        PushD        $mmgr-dealloc-return      
        Exchange                               
        StoreI                                 
        PushI        9                         
        Subtract                               
        PushD        $mmgr-dealloc-block       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-dealloc-return      
        LoadI                                  
        Return                                 
        Label        -mem-manager-remove-block 
        DLabel       $mmgr-remove-return       
        DataZ        4                         
        DLabel       $mmgr-remove-block        
        DataZ        4                         
        DLabel       $mmgr-remove-prev         
        DataZ        4                         
        DLabel       $mmgr-remove-next         
        DataZ        4                         
        PushD        $mmgr-remove-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-prev         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-next         
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-prev 
        PushD        $mmgr-remove-prev         
        LoadI                                  
        JumpFalse    -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $mmgr-remove-prev         
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Jump         -mmgr-remove-process-next 
        Label        -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-next 
        PushD        $mmgr-remove-next         
        LoadI                                  
        JumpFalse    -mmgr-remove-done         
        PushD        $mmgr-remove-prev         
        LoadI                                  
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-done         
        PushD        $mmgr-remove-return       
        LoadI                                  
        Return                                 
        DLabel       $heap-memory              
