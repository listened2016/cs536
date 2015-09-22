//Usercode
class IntLit {
}
%%
SLASH     = [/]
STAR      = [*]

%type IntLit
%function next_token;

%%

{SLASH}{STAR}[^(*/)]*{STAR}{SLASH} {System.out.println("Fuck yes!");}
