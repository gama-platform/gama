bits 64
org 0x08048000

ehdr:                                     ; Elf64_Ehdr
    db  0x7F, "ELF", 2, 1, 1, 0   ;   e_ident
    times 8 db  0
    dw  2                         ;   e_type
    dw  62                        ;   e_machine
    dd  1                         ;   e_version
    dq  _start                    ;   e_entry
    dq  phdr - $$                 ;   e_phoff
    dq  0                         ;   e_shoff
    dd  0                         ;   e_flags
    dw  ehdrsize                  ;   e_ehsize
    dw  phdrsize                  ;   e_phentsize
    dw  1                         ;   e_phnum
    dw  0                         ;   e_shentsize
    dw  0                         ;   e_shnum
    dw  0                         ;   e_shstrndx

ehdrsize equ $ - ehdr

phdr:                                     ; Elf64_Phdr
    dd  1                         ;   p_type
    dd  7                         ;   p_flags
    dq  0                         ;   p_offset
    dq  $$                        ;   p_vaddr
    dq  $$                        ;   p_paddr
    dq  filesize                  ;   p_filesz
    dq  filesize                  ;   p_memsz
    dq  0x1000                    ;   p_align

phdrsize    equ     $ - phdr

_start:
    ; create an unpredictable suffix for a private runtime directory
    mov rax, 318 ; getrandom syscall
    lea rdi, [rel random_bytes]
    mov rsi, 16
    xor rdx, rdx
    syscall

    cmp rax, 16
    jne .exit_with_error

    lea rsi, [rel random_bytes]
    lea rdi, [rel random_suffix]
    lea rbx, [rel hex_digits]
    mov ecx, 16
.generate_suffix:
    movzx eax, byte [rsi]
    mov edx, eax
    shr eax, 4
    and edx, 0x0f
    mov al, [rbx + rax]
    mov [rdi], al
    inc rdi
    mov dl, [rbx + rdx]
    mov [rdi], dl
    inc rdi
    inc rsi
    loop .generate_suffix

    lea rdi, [rel tmp_path]
    lea rsi, [rel random_suffix]
    call .fill_path_suffix
    lea rdi, [rel extractor_path]
    lea rsi, [rel random_suffix]
    call .fill_path_suffix
    lea rdi, [rel zip_path]
    lea rsi, [rel random_suffix]
    call .fill_path_suffix
    lea rdi, [rel zip_extracted_path]
    lea rsi, [rel random_suffix]
    call .fill_path_suffix
    lea rdi, [rel gama_entrypoint_path]
    lea rsi, [rel random_suffix]
    call .fill_path_suffix

    ; check tmp directory
    mov rax, 0x53 ; mkdir syscall
    lea rdi, [rel tmp_path]
    mov rsi,700o
    syscall

    test rax, rax
    js .exit_with_error

    ; check the desired directory where the zip will be extracted
    mov rax, 0x53 ; mkdir syscall
    lea rdi, [rel zip_extracted_path]
    mov rsi,700o
    syscall

    test rax, rax
    js .exit_with_error

    ; open extractor file
    mov rax, 0x02
    lea rdi, [rel extractor_path]
    mov rsi, 0x41 ; O_WRONLY | O_CREAT
    mov rdx, 777o ; RWX for UGO
    syscall
    mov rdi, rax

    cmp rdi, 0
    jl .exit_with_error
  
    ;  write the extractor on disk
    mov rax, 0x1
    lea rsi, [rel data]
    mov rdx, [rel extractor_content_size]
    syscall

    ; close extractor file descriptor
    mov rax, 0x3
    syscall

    ; open zip file
    mov rax, 0x02
    lea rdi, [rel zip_path]
    mov rsi, 0x41 ; O_WRONLY | O_CREAT
    mov rdx, 777o ; RWX for UGO
    syscall
    mov rdi, rax

    cmp rdi, 0
    jl .exit_with_error

    ; write the zip on disk
    mov rax, 0x1
    lea rsi, [rel data]
    mov rcx, [rel zip_content_offset]
    add rsi, rcx
    mov rdx, [rel zip_content_size]
    syscall

    ; close extractor file descriptor
    mov rax, 0x3
    syscall

    ; forking and executing the extractor
    mov rax, 0x39
    syscall
 
    test rax,rax
    jz .execve_extractor ; child does the execve
    ; jmp .execve_extractor
    ; parent waits
    mov rdi, rax
    mov rax, 0x3d ; syscall wait4
    xor rsi, rsi
    xor rdx, rdx
    xor r10, r10
    syscall
    
; executing Gama
.start_gama:

    mov rax, 0x3b
    lea rdi, [rel gama_entrypoint_path]
    lea rsi, [rel gama_args]

    ; resolve envp
    mov rcx, [rsp]              ; rcx = argc
    lea rdx, [rsp + rcx*8 + 16] ; rdx = pointer to envrionment variables
    
    syscall

.exit_with_error:
    mov rax, 0x3c
    mov rdi, 0x1
    syscall

.fill_path_suffix:
.find_path_suffix:
    cmp byte [rdi], 'X'
    jne .advance_path_suffix
    mov rax, rdi
    mov ecx, 32
.check_path_suffix:
    cmp byte [rax], 'X'
    jne .advance_path_suffix
    inc rax
    loop .check_path_suffix
    jmp .copy_path_suffix
.advance_path_suffix:
    inc rdi
    jmp .find_path_suffix
.copy_path_suffix:
    mov ecx, 32
.copy_path_suffix_loop:
    mov al, [rsi]
    mov [rdi], al
    inc rsi
    inc rdi
    loop .copy_path_suffix_loop
    ret

 .execve_extractor:
    mov rax, 0x3b
    lea rdi, [rel extractor_path]
    lea rsi, [rel extractor_args]
    xor rdx,rdx
    syscall

db "__DATA_START__",0
tmp_path: times 1024 db 0
extractor_path: times 1024 db 0
zip_path: times 1024 db 0
zip_extracted_path: times 1024 db 0
gama_entrypoint_path: times 1024 db 0
extractor_content_size: dq 0
zip_content_offset: dq 0
zip_content_size: dq 0

; the following values are  hard coded in this file
extractor_args: dq extractor_path,zip_path,zip_extracted_path,0
gama_args: dq gama_entrypoint_path,0

random_bytes: times 16 db 0
random_suffix: times 32 db 0
hex_digits: db "0123456789abcdef"

filesize equ $ - $$
data: