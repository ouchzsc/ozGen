local util = {}

function util.setMetaGet(data, name2index, name2ref)
    local theMetaTable = {
        __index = function(oneLineData, name)
            local index = name2index[name]
            if index then
                return rawget(oneLineData, index)
            end
            if not name2ref then
                return
            end
            local index_tb = name2ref[name]
            if index_tb then
                return index_tb[2](rawget(oneLineData, index_tb[1]))
            end
        end
    }
    for _, v in pairs(data) do
        setmetatable(v, theMetaTable)
    end
end

return util