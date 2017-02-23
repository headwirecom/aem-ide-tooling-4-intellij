### Lib Folder

This folder contains all the files copied down from Maven. To clean it
up you need to go to the Project Modules and go through all Libraries
especially in the Maven one as they contains references to one or many
artifacts.

## Jackrabbit Vault HW 3.0.0

Jackrabbit Vault's org.apache.jackrabbit.vault.fs.impl.AggregateManagerImpl.mount() contains an assertion (line 153) that fails with an NPE (!!!).

Because IntelliJ run with flag '-ae' out of the box (assertion enabled) we had to remove this line. The Version org.apache.jackrabbit.vault-HW-3.0.0.jar contains that patch.

Jackrabbit team is aware of that and it might be fixed in 3.1.X but for now we need to keep this patch here otherwise the **Import from Server** will fail.

Andreas Schaefer, 3/11/2016s